package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.service.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.service.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.robocopy.RobocopyService;
import com.go2wheel.mysqlbackup.service.robocopy.RobocopyService.SSHPowershellInvokeResult;
import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.BorgListResult;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Controller
@RequestMapping(RobocopyController.MAPPING_PATH)
public class RobocopyController extends ControllerBase {
	
	public static final String MAPPING_PATH = "/app/robocopy";
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private BorgService borgService;
	
	@Autowired
	private RobocopyService robocopyService;
	
	@Autowired
	private RobocopyDescriptionDbService robocopyDescriptionDbService;
	
	@Autowired
	private RobocopyItemDbService robocopyItemDbService;
	
	@Autowired
	private ServerDbService serverDbService;
	
	public RobocopyController() {
		super(MAPPING_PATH);
	}
	
	
	@GetMapping("/fullcopies/{server}")
	public String listArchive(@PathVariable(name="server") Server server, Model model, HttpServletRequest httpRequest) throws JSchException, CommandNotFoundException, IOException {
		server = serverDbService.loadFull(server);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		if (!frSession.isExpected()) {
			
		}
		Session session = frSession.getResult();
		try {
			FacadeResult<BorgListResult> fr = borgService.listArchives(session, server);
			List<String> archives = fr.getResult().getArchives().stream().map(it -> it.split("\\s+")[0]).collect(Collectors.toList());
			Collections.sort(archives, Collections.reverseOrder());
			model.addAttribute(CRUDController.LIST_OB_NAME, archives);
			model.addAttribute("server", server);
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		return "borg-archives-list";
	}
	
	@PostMapping("/increamental/{description}")
	public String increamental(@PathVariable(name="description") RobocopyDescription robocopyDescription, HttpServletRequest request, RedirectAttributes ras) throws JSchException, CommandNotFoundException, UnExpectedOutputException, IOException, UnExpectedInputException, RunRemoteCommandException, NoSuchAlgorithmException, ScpException {
		Server server = serverDbService.findById(robocopyDescription.getServerId());
		
		List<RobocopyItem> items = robocopyItemDbService.findByDescriptionId(robocopyDescription.getId());
		robocopyDescription.setRobocopyItems(items);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		Session session = frSession.getResult();
		SSHPowershellInvokeResult sshir = robocopyService.increamentalBackup(session, server, robocopyDescription, robocopyDescription.modifiItems(items));
		if (sshir.exitCode() == -1) {
			ras.addFlashAttribute("formProcessSuccessed", "任务结束。没有发现有变化的文件。");
		} else {
			FacadeResult<Path> fp = robocopyService.downloadIncreamentalArchive(session, server, robocopyDescription, items);
			if (fp.isExpected() && Files.exists(fp.getResult())) {
				ras.addFlashAttribute("formProcessSuccessed", "任务结束。生成了新的增量文件。");
			} else {
				ras.addFlashAttribute("errorMessage", "任务结束。但结果不是期盼值。" + fp.getException() != null ? fp.getException().getMessage() : "UNKNOWN.");
			}
		}
		
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		String editPage = "/app/robocopy-descriptions/" + robocopyDescription.getId() + "/edit";
		String uri = ucb.replacePath(editPage).build().toUriString();
		return "redirect:" + uri;
	}
	
	@PostMapping("/fullcopies/{description}")
	public String creatArchive(@PathVariable(name="description") RobocopyDescription robocopyDescription, HttpServletRequest request, RedirectAttributes ras) throws JSchException, CommandNotFoundException, UnExpectedOutputException, IOException {
		Server server = serverDbService.findById(robocopyDescription.getServerId());
		Long aid = GlobalStore.atomicLong.getAndIncrement();
		
		List<RobocopyItem> items = robocopyItemDbService.findByDescriptionId(robocopyDescription.getId());
		robocopyDescription.setRobocopyItems(items);

		String msgkey = getI18nedMessage("taskkey.robocopy.fullbackup", server.getHost());
		CompletableFuture<AsyncTaskValue> cf = robocopyService.fullBackupAsync(server, robocopyDescription, robocopyDescription.modifiItems(robocopyDescription.getRobocopyItems()), msgkey, aid);
		
		String sid = request.getSession(true).getId();
		SavedFuture sf = SavedFuture.newSavedFuture(aid, msgkey, cf);
		globalStore.saveFuture(sid, sf);
		
		ras.addFlashAttribute("formProcessSuccessed", "任务已异步发送，稍后会通知您。");
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		String editPage = "/app/robocopy-descriptions/" + robocopyDescription.getId() + "/edit";
		String uri = ucb.replacePath(editPage).build().toUriString();
		return "redirect:" + uri;
	}

	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}

}
