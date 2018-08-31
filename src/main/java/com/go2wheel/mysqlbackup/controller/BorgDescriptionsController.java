package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.propertyeditor.ListStringToLinesEditor;
import com.go2wheel.mysqlbackup.service.BorgDescriptionDbService;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.service.ReusableCronDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;


@Controller
@RequestMapping(BorgDescriptionsController.MAPPING_PATH)
public class BorgDescriptionsController  extends CRUDController<BorgDescription, BorgDescriptionDbService> {
	
	public static final String MAPPING_PATH = "/app/borg-descriptions";
	
	
	@Autowired
	private ReusableCronDbService reuseableCronDbService;
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private BorgService borgService;
	
    @InitBinder
    public void initBinder(WebDataBinder binder) {
    	binder.registerCustomEditor(List.class, new ListStringToLinesEditor());
    }
	
	@Autowired
	public BorgDescriptionsController(BorgDescriptionDbService dbService) {
		super(BorgDescription.class, dbService, MAPPING_PATH);
	}

	@Override
	boolean copyProperties(BorgDescription entityFromForm, BorgDescription entityFromDb) {
		entityFromDb.setArchiveCron(entityFromForm.getArchiveCron());
		entityFromDb.setArchiveFormat(entityFromForm.getArchiveFormat());
		entityFromDb.setArchiveNamePrefix(entityFromForm.getArchiveNamePrefix());
		entityFromDb.setPruneCron(entityFromForm.getPruneCron());
		entityFromDb.setRepo(entityFromForm.getRepo());
		entityFromDb.setIncludes(entityFromForm.getIncludes());
		entityFromDb.setExcludes(entityFromForm.getExcludes());
		entityFromDb.setLocalBackupCron(entityFromForm.getLocalBackupCron());
		entityFromDb.setLocalBackupPruneCron(entityFromForm.getLocalBackupPruneCron());
		entityFromDb.setPruneStrategy(entityFromForm.getPruneStrategy());
		return true;
	}
	
	@GetMapping("/create")
	@Override
	String getCreate(Model model, HttpServletRequest httpRequest) {
		String serverId = httpRequest.getParameter("server");
		if (serverId == null) {
			return redirectMappingUrl();
		}
		BorgDescription mi = getDbService().findByServerId(serverId);
		if (mi != null) {
			return redirectEditGet(mi.getId());
		}
		mi = newModel();
		mi.setServerId(Integer.parseInt(serverId));
		model.addAttribute(OB_NAME, mi);
		model.addAttribute("editting", false);
		commonAttribute(model);
		formAttribute(model);
		return getFormTpl();
	}
	
	
	@PostMapping("/{borgdescription}/download")
	public String postDownloads(@PathVariable(name = "borgdescription") BorgDescription borgDescription, Model model, HttpServletRequest request, RedirectAttributes ras) {
		Server server = serverDbService.findById(borgDescription.getServerId());
		server = serverDbService.loadFull(server);
		
		Long aid = GlobalStore.atomicLong.getAndIncrement();
		
		String msgkey = getI18nedMessage(BorgService.BORG_DOWNLOAD_TASK_KEY, server.getHost());
		
		CompletableFuture<AsyncTaskValue> cf = borgService.downloadRepoAsync(server, msgkey, aid);
		String sid = request.getSession(true).getId();
		
		SavedFuture sf = SavedFuture.newSavedFuture(aid, msgkey, cf);
		
		globalStore.saveFuture(sid, sf);
		
		ras.addFlashAttribute("formProcessSuccessed", "任务已异步发送，稍后会通知您。");
		return redirectMappingUrl();
	}

	@PostMapping("/{borgdescription}/bk-local-repo")
	public String postBackupLocalRepo(@PathVariable(name = "borgdescription") BorgDescription borgDescription, Model model, HttpServletRequest request, RedirectAttributes ras) throws IOException {
		Server server = serverDbService.findById(borgDescription.getServerId());
		borgService.backupLocalRepos(server);
		ras.addFlashAttribute("formProcessSuccessed", "任务已异步发送，稍后会通知您。");
		return redirectMappingUrl();
	}
	
	@Override
	protected String afterCreate(BorgDescription entityFromForm) {
		return "redirect:" + MAPPING_PATH + "/" + entityFromForm.getId() + "/edit";
	}

	@Override
	public BorgDescription newModel() {
		return new BorgDescription.BorgDescriptionBuilder(0).build();
	}

	@Override
	protected void formAttribute(Model model) {
		model.addAttribute("crons", reuseableCronDbService.findAll());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void listExtraAttributes(Model model) {
		List<BorgDescription> mis = (List<BorgDescription>) model.asMap().get(LIST_OB_NAME);
		List<Server> servers = serverDbService.findByIds(mis.stream().map(BorgDescription::getServerId).toArray(size -> new Integer[size]));
		model.addAttribute(ID_ENTITY_MAP, servers.stream().collect(Collectors.toMap(Server::getId, s -> s)));
	}
	
	protected int getMenuOrder() {
		return 600;
	}
}
