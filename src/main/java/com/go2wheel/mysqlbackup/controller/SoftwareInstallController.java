package com.go2wheel.mysqlbackup.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.installer.Installer;
import com.go2wheel.mysqlbackup.installer.MySqlInstaller;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.service.SoftwareDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.google.common.collect.Maps;
import com.jcraft.jsch.JSchException;

@Controller
@RequestMapping(SoftwareInstallController.MAPPING_PATH)
public class SoftwareInstallController extends ControllerBase {
	
	
	public static final String MAPPING_PATH = "/app/software-install";

	@Autowired
	private SoftwareDbService softwareDbService;
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	private List<Installer<?>> installers;
	
	public SoftwareInstallController() {
		super(MAPPING_PATH);
	}
	
	@Autowired
	public void setInstallers(List<Installer<?>> installers) {
		this.installers = installers;
	}

	@GetMapping("/{server}")
	public String getInstall(@PathVariable(name = "server") Server server,
			@RequestParam(required = false) Software software, Model model, HttpServletRequest httpRequest) {
		model.addAttribute("server", server);
		List<Software> softwares = softwareDbService.findAll();
		if (software == null && softwares.size() > 0) {
			software = softwares.get(0);
		}
		model.addAttribute("software", software);
		model.addAttribute("softwares", softwares);
		
		List<Software> installed = softwareDbService.findByServer(server);
		model.addAttribute("listItems", installed);
		return "software-install";
	}
	
	@DeleteMapping("")
	public String unInstall(@RequestParam Server server,
			@RequestParam Software software, HttpServletRequest request) {
		
		String msgkey = getI18nedMessage(MySqlInstaller.TASK_KEY, server.getHost());
		Long aid = GlobalStore.atomicLong.getAndIncrement();
		
		for(Installer<?> il: installers) {
			if(il.canHandle(software)) {
				CompletableFuture<AsyncTaskValue> cf = il.uninstallAsync(server, software, msgkey, aid);
				String sid = request.getSession(true).getId();
				SavedFuture sf = SavedFuture.newSavedFuture(aid, msgkey, cf);
				globalStore.saveFuture(sid, sf);
			}
		}
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		Map<String, Object> tmap = Maps.newHashMap();
		tmap.put("server", server.getId());
		
		ucb.replacePath("/app/software-install/" + server.getId()).build();
		String url = ucb.toUriString();
		return "redirect:" + url;
	}

	@PostMapping("/{server}")
	public String install(@PathVariable(name = "server") Server server, @RequestParam Software software, Model model,
			HttpServletRequest request, RedirectAttributes ras) throws UnsupportedEncodingException, RunRemoteCommandException, JSchException {
		Map<String, String[]> parameterMap = request.getParameterMap();
		Map<String, String> parameters = parameterMap.entrySet().stream().filter(es -> es.getValue().length > 0)
				.collect(Collectors.toMap(es -> es.getKey(), es -> es.getValue()[0]));
		
		String msgkey = getI18nedMessage(MySqlInstaller.TASK_KEY, server.getHost());
		
		Long aid = GlobalStore.atomicLong.getAndIncrement();
		
		for(Installer<?> il: installers) {
			if(il.canHandle(software)) {
				SSHcommonUtil.echo(sshSessionFactory, server);
				CompletableFuture<AsyncTaskValue> cf = il.installAsync(server, software, msgkey, aid, parameters);
				String sid = request.getSession(true).getId();
				SavedFuture sf = SavedFuture.newSavedFuture(aid, msgkey, cf);
				globalStore.saveFuture(sid, sf);
			}
		}
		ras.addFlashAttribute("formProcessSuccessed", "任务已异步发送，稍后会通知您。");
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		String uri = ucb.replaceQueryParam("software", software.getId()).build().toUriString();
		return "redirect:" + uri;
	}
	
	@GetMapping("/systems")
	@ResponseBody
	public Properties allSystemProperties() {
		return System.getProperties();
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
