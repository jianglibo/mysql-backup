package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.service.MysqlFlushDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlDumpFolder;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Controller
@RequestMapping(MysqlController.MAPPING_PATH)
public class MysqlController extends ControllerBase {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String MAPPING_PATH = "/app/mysql";

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Autowired
	private MysqlService mysqlService;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private MysqlFlushDbService mysqlFlushDbService;

	public MysqlController() {
		super(MAPPING_PATH);
	}

	@GetMapping("/{server}/dumps")
	public String getDumps(@PathVariable(name = "server") Server server, Model model, HttpServletRequest request)
			throws IOException {
		Path dumps = settingsInDb.getCurrentDumpDir(server).getParent();
		List<MysqlDumpFolder> dumpFolders = Files.list(dumps).map(MysqlDumpFolder::newInstance).filter(Objects::nonNull)
				.collect(Collectors.toList());
		Collections.sort(dumpFolders);
		model.addAttribute(CRUDController.LIST_OB_NAME, dumpFolders);
		model.addAttribute("server", server);
		model.addAttribute("mapping", MAPPING_PATH);
		return "mysql-dumps-list";
	}

	@PostMapping("/{server}/dumps")
	public String postDumps(@PathVariable(name = "server") Server server, Model model, HttpServletRequest request,
			RedirectAttributes ras)
			throws JSchException, IOException, NoSuchAlgorithmException, UnExpectedContentException {
		model.asMap().clear();
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		server = serverDbService.loadFull(server);

		String sid = request.getSession(true).getId();
		String msgkey = getI18nedMessage(MysqlService.DUMP_TASK_KEY, new Object[] { server.getHost() });
		
		Long aid = GlobalStore.atomicLong.getAndIncrement();

		CompletableFuture<AsyncTaskValue> cf = mysqlService.mysqlDumpAsync(server, msgkey, aid);
		
		SavedFuture sf = SavedFuture.newSavedFuture(aid, msgkey, cf);

		globalStore.saveFuture(sid, sf);

		ras.addFlashAttribute("formProcessSuccessed", "任务已异步发送，稍后会通知您。");
		return "redirect:" + ucb.build().toUriString();
	}

	@PostMapping("/{server}/flushes")
	public String postFlushes(@PathVariable(name = "server") Server server, Model model, HttpServletRequest request)
			throws JSchException, IOException, NoSuchAlgorithmException, UnExpectedInputException,
			UnExpectedContentException, MysqlAccessDeniedException {
		model.asMap().clear();
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		server = serverDbService.loadFull(server);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		Session session = frSession.getResult();
		try {
			FacadeResult<Path> fr = mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
			mysqlFlushDbService.processFlushResult(server, fr);
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		return "redirect:" + ucb.replacePath(request.getRequestURI().replace("flushes", "dumps")).build().toUriString();
	}

	@PutMapping("/logbin/{server}")
	public String updateLogBin(@PathVariable(name = "server") Server server, Model model, HttpServletRequest request,
			RedirectAttributes ras) throws JSchException, UnExpectedContentException, MysqlAccessDeniedException {
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		server = serverDbService.loadFull(server);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		Session session = frSession.getResult();
		try {
			FacadeResult<?> fr;
			if (server.getMysqlInstance().getLogBinSetting().isEnabled()) {
				fr = mysqlService.disableLogbin(session, server);
			} else {
				fr = mysqlService.enableLogbin(session, server, MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME);
			}

			if (!fr.isExpected()) {
				ras.addFlashAttribute(CRUDController.ERROR_MESSAGE_KEY, fr.getMessage());
			}
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		String uri = ucb.replacePath("/app/mysql-instances/" + server.getMysqlInstance().getId() + "/edit").build()
				.toUriString();
		return "redirect:" + uri;
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
