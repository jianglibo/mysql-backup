package com.go2wheel.mysqlbackup.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.BorgListResult;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;

@Controller
@RequestMapping("/app/mysql")
public class MysqlController extends ControllerBase {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private BorgService borgService;
	
	@Autowired
	private MysqlService mysqlService;
	
	@Autowired
	private ServerDbService serverDbService;
	
    @ExceptionHandler
    public ResponseEntity<String> handle(Exception ex) {
    	ExceptionUtil.logErrorException(logger, ex);
        return ResponseEntity.ok("OK");
    }
	
//	borg info /path/to/repo::2017-06-29T11:00-srv
	
	@GetMapping("/info/{server}/{archive}")
	@ResponseBody
	public String infoArchive(@PathVariable(name="server") Server server, @PathVariable String archive, Model model, HttpServletRequest httpRequest) {
		server = serverDbService.loadFull(server);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		Session session = frSession.getResult();
		try {
			FacadeResult<List<String>> fr = borgService.archiveInfo(session, server, archive);
			return fr.getResult().stream().collect(Collectors.joining("\n"));
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
	}
	
	
	@GetMapping("/archives/{server}")
	public String listArchive(@PathVariable(name="server") Server server, Model model, HttpServletRequest httpRequest) {
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
	
	@PutMapping("/logbin/{server}")
	public String pruneArchive(@PathVariable(name="server") Server server,Model model,  HttpServletRequest request, RedirectAttributes ras) {
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		server = serverDbService.loadFull(server);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		Session session = frSession.getResult();
		try {
			FacadeResult<?> fr = mysqlService.enableLogbin(session, server, MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME);
			if (!fr.isExpected()) {
				
			}
		} catch (MysqlAccessDeniedException me) {
			ras.addFlashAttribute(CRUDController.ERROR_MESSAGE_KEY, CommonMessageKeys.AUTHENTICATION_FAILED);
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		String uri = ucb.replacePath("/app/mysql-instances/" + server.getId() + "/edit").build().toUriString();
		return "redirect:" + uri;
	}
	
	@PostMapping("/archives/{server}")
	public String creatArchive(@PathVariable(name="server") Server server, HttpServletRequest request) {
		server = serverDbService.loadFull(server);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		Session session = frSession.getResult();
		try {
			FacadeResult<RemoteCommandResult> fr = borgService.archive(frSession.getResult(), server, true);
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		String uri = ucb.build().toUriString();
		return "redirect:" + uri;
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
