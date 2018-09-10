package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
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
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.service.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.robocopy.RobocopyService;
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
	private ServerDbService serverDbService;
	
	public RobocopyController() {
		super(MAPPING_PATH);
	}
	
//	borg info /path/to/repo::2017-06-29T11:00-srv
	
	@GetMapping("/info/{server}/{archive}")
	@ResponseBody
	public String infoArchive(@PathVariable(name="server") Server server, @PathVariable String archive, Model model, HttpServletRequest httpRequest) throws JSchException, IOException {
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
	
	@PutMapping("/archives/{server}")
	public String pruneArchive(@PathVariable(name="server") Server server,  HttpServletRequest request) throws JSchException, UnExpectedContentException, IOException {
		server = serverDbService.loadFull(server);
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
		Session session = frSession.getResult();
		try {
			FacadeResult<BorgPruneResult> fr = borgService.pruneRepo(session, server);
			if (!fr.isExpected()) {
				throw new UnExpectedContentException("10000", "borg.archive.unexpected", fr.getMessage());
			}
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		String uri = ucb.build().toUriString();
		return "redirect:" + uri;
	}
	
	@PostMapping("/fullcopies/{server}")
	public String creatArchive(@PathVariable(name="server") Server server, HttpServletRequest request, RedirectAttributes ras) throws JSchException, CommandNotFoundException, UnExpectedContentException, IOException {
		RobocopyDescription robocopyDescription = robocopyDescriptionDbService.findByServerId(server.getId());
		
		Long aid = GlobalStore.atomicLong.getAndIncrement();

		String msgkey = getI18nedMessage("taskkey.robocopy.fullbackup", server.getHost());
		CompletableFuture<AsyncTaskValue> cf = robocopyService.fullBackupAsync(server, robocopyDescription, robocopyDescription.modifiItems(robocopyDescription.getRobocopyItems()), msgkey, aid);
		
		String sid = request.getSession(true).getId();
		SavedFuture sf = SavedFuture.newSavedFuture(aid, msgkey, cf);
		globalStore.saveFuture(sid, sf);
		
		ras.addFlashAttribute("formProcessSuccessed", "任务已异步发送，稍后会通知您。");
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		String uri = ucb.build().toUriString();
		return "redirect:" + uri;
	}

	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}

}
