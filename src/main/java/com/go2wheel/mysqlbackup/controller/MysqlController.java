package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.MysqlDumpDbService;
import com.go2wheel.mysqlbackup.service.MysqlFlushDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
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
	private MysqlDumpDbService mysqlDumpDbService;
	
	@Autowired
	private MysqlFlushDbService mysqlFlushDbService;
	
	public MysqlController() {
		super(MAPPING_PATH);
	}

	@ExceptionHandler
	public ResponseEntity<String> handle(Exception ex) {
		ExceptionUtil.logErrorException(logger, ex);
		return ResponseEntity.ok("OK");
	}

	@GetMapping("/{server}/dumps")
	public String getDumps(@PathVariable(name = "server") Server server, Model model, HttpServletRequest request) {
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		server = serverDbService.loadFull(server);
		List<MysqlDump> dumps = mysqlDumpDbService.findAll(
				com.go2wheel.mysqlbackup.jooqschema.tables.MysqlDump.MYSQL_DUMP.SERVER_ID.eq(server.getId()), 0, 50);
		model.addAttribute(CRUDController.LIST_OB_NAME, dumps);
		model.addAttribute("server", server);
		model.addAttribute("mapping", ucb.build().toUriString());
		return "mysql-dumps-list";
	}

	@PostMapping("/{server}/dumps")
	public String postDumps(@PathVariable(name = "server") Server server, Model model, HttpServletRequest request) {
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		server = serverDbService.loadFull(server);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		Session session = frSession.getResult();
		try {
			FacadeResult<LinuxLsl> lsl = mysqlService.mysqlDump(session, server, true);
			mysqlService.saveDumpResult(server, lsl);
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		return "redirect:" + ucb.build().toUriString();
	}

	@GetMapping("/{server}/flushes")
	public String getFlushes(@PathVariable(name = "server") Server server, Model model, HttpServletRequest request) {
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		server = serverDbService.loadFull(server);
		List<MysqlFlush> flushes = mysqlFlushDbService.findAll(
				com.go2wheel.mysqlbackup.jooqschema.tables.MysqlFlush.MYSQL_FLUSH.SERVER_ID.eq(server.getId()), 0, 50);
		model.addAttribute(CRUDController.LIST_OB_NAME, flushes);
		model.addAttribute("server", server);
		model.addAttribute("mapping", ucb.build().toUriString());
		return "mysql-flushes-list";
	}

	@PostMapping("/{server}/flushes")
	public String postFlushes(@PathVariable(name = "server") Server server, Model model, HttpServletRequest request) {
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		server = serverDbService.loadFull(server);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		Session session = frSession.getResult();
		try {
			FacadeResult<String> fr = mysqlService.mysqlFlushLogs(session, server);
			mysqlFlushDbService.processFlushResult(server, fr);
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		return "redirect:" + ucb.build().toUriString();
	}

	@PutMapping("/logbin/{server}")
	public String updateLogBin(@PathVariable(name = "server") Server server, Model model, HttpServletRequest request,
			RedirectAttributes ras) {
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
		} catch (MysqlAccessDeniedException me) {
			ras.addFlashAttribute(CRUDController.ERROR_MESSAGE_KEY, CommonMessageKeys.AUTHENTICATION_FAILED);
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		String uri = ucb.replacePath("/app/mysql-instances/" + server.getMysqlInstance().getId() + "/edit").build().toUriString();
		return "redirect:" + uri;
	}


	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
