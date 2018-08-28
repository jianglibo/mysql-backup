package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;

@Controller
@RequestMapping(BorgRestoreConstroller.MAPPING_PATH)
public class BorgRestoreConstroller extends ControllerBase {

	public static final String MAPPING_PATH = "/app/borgrestore";

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private BorgService borgService;

	public BorgRestoreConstroller() {
		super(MAPPING_PATH);
	}

	@GetMapping("/{playback}")
	public String listLocalRepos(@PathVariable PlayBack playback, Model model, HttpServletRequest request)
			throws IOException {
		Server sourceServer = serverDbService.findById(playback.getSourceServerId());
		model.addAttribute(CRUDController.LIST_OB_NAME, borgService.listLocalRepos(sourceServer));
		return "borg-restore-list";
	}

	@PostMapping("/{playback}")
	public String restore(@PathVariable PlayBack playback, @RequestParam(name = "repo") String repo, Model model,
			HttpServletRequest request, RedirectAttributes ras) {
		
		Long aid = GlobalStore.atomicLong.getAndIncrement();
		CompletableFuture<AsyncTaskValue> cf  = borgService.playbackAsync(playback, repo, aid);
		String msgkey = getI18nedMessage(BorgService.BORG_RESTORE_TASK_KEY, playback.getSourceServerId(), playback.getTargetServerId());
		String sid = request.getSession(true).getId();
		SavedFuture sf = SavedFuture.newSavedFuture(aid, msgkey, cf);
		globalStore.saveFuture(sid, sf);
		ras.addFlashAttribute("formProcessSuccessed", "任务已异步发送，稍后会通知您。");
		return "redirect:" + MAPPING_PATH + "/" + playback.getId();
	}

	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}

}
