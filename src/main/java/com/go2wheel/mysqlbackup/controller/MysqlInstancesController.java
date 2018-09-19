package com.go2wheel.mysqlbackup.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.go2wheel.mysqlbackup.event.ModelPreCreatedEvent;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.propertyeditor.ListStringToLinesEditor;
import com.go2wheel.mysqlbackup.service.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.service.ReusableCronDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;
import com.go2wheel.mysqlbackup.util.StringUtil;


@Controller
@RequestMapping(MysqlInstancesController.MAPPING_PATH)
public class MysqlInstancesController  extends CRUDController<MysqlInstance, MysqlInstanceDbService> {
	
	public static final String MAPPING_PATH = "/app/mysql-instances";
	
	@Autowired
	private ReusableCronDbService reuseableCronDbService;
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	public MysqlInstancesController(MysqlInstanceDbService dbService) {
		super(MysqlInstance.class, dbService, MAPPING_PATH);
	}
	
    @InitBinder
    public void initBinder(WebDataBinder binder) {
    	binder.registerCustomEditor(List.class, new ListStringToLinesEditor());
    }

	@Override
	boolean copyProperties(MysqlInstance entityFromForm, MysqlInstance entityFromDb) {
		entityFromDb.setHost(entityFromForm.getHost());
		entityFromDb.setPort(entityFromForm.getPort());
		entityFromDb.setUsername(entityFromForm.getUsername());
		entityFromDb.setPassword(entityFromForm.getPassword());
		entityFromDb.setDumpFileName(entityFromForm.getDumpFileName());
		entityFromDb.setClientBin(entityFromForm.getClientBin());
		entityFromDb.setFlushLogCron(entityFromForm.getFlushLogCron());
		entityFromDb.setRestartCmd(entityFromForm.getRestartCmd());
		return true;
	}
	
	@Override
	protected String afterEdit(HttpServletRequest request, RedirectAttributes ras) {
		return redirectEditUrl();
	}
	
	@GetMapping("/{mysqlInstance}/viewdumps")
	String viewDumps(@PathVariable MysqlInstance mysqlInstance, Model model, HttpServletRequest httpRequest) {
		model.asMap().clear();
		Integer sid = mysqlInstance.getServerId();
		return "redirect:" + MysqlController.MAPPING_PATH + "/" + sid + "/dumps";
	}
	
	@GetMapping("/create")
	@Override
	String getCreate(Model model, HttpServletRequest httpRequest) {
		String serverId = httpRequest.getParameter("server");
		if (serverId == null) {
			return redirectListingUrl(httpRequest);
		}
		
		Server server = serverDbService.findById(serverId);
		MysqlInstance mi = getDbService().findByServerId(serverId);
		if (mi != null) {
			model.asMap().clear();
			return redirectEditUrl(mi.getId());
		}
		mi = newModel();
		mi.setDumpFileName(MysqlInstance.getDefaultDumpFileName(server.getOs()));
		mi.setRestartCmd(MysqlInstance.getDefaultRestartCmd(server.getOs()));
		mi.setServerId(Integer.parseInt(serverId));
		model.addAttribute(OB_NAME, mi);
		model.addAttribute("editting", false);
		commonAttribute(model);
		formAttribute(model);
		return getFormTpl();
	}

	@Override
	protected String afterCreate(MysqlInstance savedEntity, HttpServletRequest request) {
	    return redirectEditUrl(savedEntity.getId());
	}
	
	@Override
	public MysqlInstance newModel() {
		return new MysqlInstance.MysqlInstanceBuilder(0, "", MysqlInstance.DEFAULT_CLIENT_BIN, "", "").build();
	}

	@Override
	protected void formAttribute(Model model) {
		model.addAttribute("crons", reuseableCronDbService.findAll());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void listExtraAttributes(Model model) {
		List<MysqlInstance> mis = (List<MysqlInstance>) model.asMap().get(LIST_OB_NAME);
		List<Server> servers = serverDbService.findByIds(mis.stream().map(MysqlInstance::getServerId).toArray(size -> new Integer[size]));
		model.addAttribute(ID_ENTITY_MAP, servers.stream().collect(Collectors.toMap(Server::getId, s -> s)));
	}
	
	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}
	
	
	@EventListener
	public void whenMysqlInstanceBeforeCreated(ModelPreCreatedEvent<MysqlInstance> mysqlInstanceCreatedEvent) {
		MysqlInstance mi = mysqlInstanceCreatedEvent.getModel();
		if (!StringUtil.hasAnyNonBlankWord(mi.getDumpFileName())) {
			Server server = serverDbService.findById(mi.getServerId());
			mi.setDumpFileName(MysqlInstance.getDefaultDumpFileName(server.getOs()));
		}
		
		if (!StringUtil.hasAnyNonBlankWord(mi.getClientBin())) {
			mi.setClientBin("mysql");
		}
	}
}
