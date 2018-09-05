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
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.propertyeditor.ListStringToLinesEditor;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.service.ReusableCronDbService;
import com.go2wheel.mysqlbackup.service.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;


@Controller
@RequestMapping(RobocopyDescriptionsController.MAPPING_PATH)
public class RobocopyDescriptionsController  extends CRUDController<RobocopyDescription, RobocopyDescriptionDbService> {
	
	public static final String MAPPING_PATH = "/app/robocopy-descriptions";
	
	
	@Autowired
	private ReusableCronDbService reuseableCronDbService;
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private BorgService borgService;
	
    @InitBinder
    public void initBinder(WebDataBinder binder) {
    	binder.registerCustomEditor(List.class, new ListStringToLinesEditor());
    }
	
	@Autowired
	public RobocopyDescriptionsController(RobocopyDescriptionDbService dbService) {
		super(RobocopyDescription.class, dbService, MAPPING_PATH);
	}

	@Override
	boolean copyProperties(RobocopyDescription entityFromForm, RobocopyDescription entityFromDb) {
		entityFromDb.setInvokeCron(entityFromForm.getInvokeCron());
		entityFromDb.setLocalBackupCron(entityFromForm.getLocalBackupCron());
		entityFromDb.setPruneStrategy(entityFromForm.getPruneStrategy());
		entityFromDb.setRepo(entityFromForm.getRepo());
		return true;
	}
	
	@GetMapping("/create")
	@Override
	String getCreate(Model model, HttpServletRequest httpRequest) {
		String serverId = httpRequest.getParameter("server");
		if (serverId == null) {
			return redirectMappingUrl();
		}
		RobocopyDescription mi = getDbService().findByServerId(serverId);
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
	
	
	@PostMapping("/{RobocopyDescription}/download")
	public String postDownloads(@PathVariable(name = "RobocopyDescription") RobocopyDescription RobocopyDescription, Model model, HttpServletRequest request, RedirectAttributes ras) {
		Server server = serverDbService.findById(RobocopyDescription.getServerId());
		server = serverDbService.loadFull(server);
		
		Long aid = GlobalStore.atomicLong.getAndIncrement();
		
		String msgkey = getI18nedMessage(BorgService.BORG_DOWNLOAD_TASK_KEY, server.getHost());
		
		CompletableFuture<AsyncTaskValue> cf = borgService.downloadRepoAsync(server, msgkey, aid);
		String sid = request.getSession(true).getId();
		
		SavedFuture sf = SavedFuture.newSavedFuture(aid, msgkey, cf);
		
		globalStore.saveFuture(sid, sf);
		
		ras.addFlashAttribute("successMessage", "任务已异步发送，稍后会�?�知您�??");
		return redirectMappingUrl();
	}
	
//	@PostMapping("/{RobocopyDescription}/initrepo")
//	public String initRepo(@PathVariable(name = "RobocopyDescription") RobocopyDescription RobocopyDescription, Model model, HttpServletRequest request, RedirectAttributes ras) throws JSchException, CommandNotFoundException, UnExpectedContentException, IOException {
//		Server server = serverDbService.findById(RobocopyDescription.getServerId());
//		server = serverDbService.loadFull(server);
//
//		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
//		Session session = frSession.getResult();
//		try {
//			FacadeResult<RemoteCommandResult> fr = borgService.initRepo(session, server.getRobocopyDescription().getRepo());
//			if (!fr.isExpected()) {
//				if (CommonMessageKeys.OBJECT_ALREADY_EXISTS.equals(fr.getMessage())) {
//					ras.addFlashAttribute("warnMessage", "仓库之前已经初始化了�?");
//					return redirectMappingUrl();
//				} else {
//					RemoteCommandResult rcr = fr.getResult();
//					if (rcr != null) {
//						throw new UnExpectedContentException("10000", "borg.archive.unexpected", rcr.getAllTrimedNotEmptyLines().stream().collect(Collectors.joining("\n")));
//					}
//				}
//				
//			}
//		} finally {
//			if (session != null && session.isConnected()) {
//				session.disconnect();
//			}
//		}
//		ras.addFlashAttribute("successMessage", "仓库初始化完毕�??");
//		return redirectMappingUrl();
//	}

	@PostMapping("/{RobocopyDescription}/bk-local-repo")
	public String postBackupLocalRepo(@PathVariable(name = "RobocopyDescription") RobocopyDescription RobocopyDescription, Model model, HttpServletRequest request, RedirectAttributes ras) throws IOException {
		Server server = serverDbService.findById(RobocopyDescription.getServerId());
		borgService.backupLocalRepos(server);
		ras.addFlashAttribute("formProcessSuccessed", "任务已异步发送，稍后会�?�知您�??");
		return redirectMappingUrl();
	}
	
	@Override
	protected String afterCreate(RobocopyDescription entityFromForm) {
		return "redirect:" + MAPPING_PATH + "/" + entityFromForm.getId() + "/edit";
	}

	@Override
	public RobocopyDescription newModel() {
		return new RobocopyDescription.RobocopyDescriptionBuilder(0, "").build();
	}

	@Override
	protected void formAttribute(Model model) {
		model.addAttribute("crons", reuseableCronDbService.findAll());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void listExtraAttributes(Model model) {
		List<RobocopyDescription> mis = (List<RobocopyDescription>) model.asMap().get(LIST_OB_NAME);
		List<Server> servers = serverDbService.findByIds(mis.stream().map(RobocopyDescription::getServerId).toArray(size -> new Integer[size]));
		model.addAttribute(ID_ENTITY_MAP, servers.stream().collect(Collectors.toMap(Server::getId, s -> s)));
	}
	
	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}

}
