package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.propertyeditor.ListStringToLinesEditor;
import com.go2wheel.mysqlbackup.service.ReusableCronDbService;
import com.go2wheel.mysqlbackup.service.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.service.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;
import com.go2wheel.mysqlbackup.util.StringUtil;


@Controller
@RequestMapping(RobocopyItemsController.LISTING_PATH)
public class RobocopyItemsController  extends CRUDController<RobocopyItem, RobocopyItemDbService> {
	
	public static final String LISTING_PATH = "/app/robocopy-items";
	
	
	@Autowired
	private ReusableCronDbService reuseableCronDbService;
	
	@Autowired
	private RobocopyDescriptionDbService robocopyDescriptionDbService;
	
    @InitBinder
    public void initBinder(WebDataBinder binder) {
    	binder.registerCustomEditor(List.class, new ListStringToLinesEditor());
    }
	
	@Autowired
	public RobocopyItemsController(RobocopyItemDbService dbService) {
		super(RobocopyItem.class, dbService, LISTING_PATH);
	}

	@Override
	boolean copyProperties(RobocopyItem entityFromForm, RobocopyItem entityFromDb) {
		entityFromDb.setCopyOptions(entityFromForm.getCopyOptions());
		entityFromDb.setDstRelative(entityFromForm.getDstRelative());
		entityFromDb.setExcludeDirectories(entityFromForm.getExcludeDirectories());
		entityFromDb.setExcludeFiles(entityFromForm.getExcludeFiles());
		entityFromDb.setFileParameters(entityFromForm.getFileParameters());
		entityFromDb.setFileSelectionOptions(entityFromForm.getFileSelectionOptions());
		entityFromDb.setJobOptions(entityFromForm.getJobOptions());
		entityFromDb.setLoggingOptions(entityFromForm.getLoggingOptions());
		entityFromDb.setRetryOptions(entityFromForm.getRetryOptions());
		entityFromDb.setSource(entityFromForm.getSource());
		return true;
	}
	
	@GetMapping("/create")
	@Override
	String getCreate(Model model, HttpServletRequest request) {
		String descriptionId = request.getParameter("descriptionId");
		if (!StringUtil.isAllDigitsAndNotEmpty(descriptionId)) {
			return redirectListingUrl(request);
		}
		RobocopyDescription rd = robocopyDescriptionDbService.findById(descriptionId);
		RobocopyItem ri = newModel();
		ri.setDescriptionId(rd.getId());
		model.addAttribute(OB_NAME, ri);
		model.addAttribute("editting", false);
		commonAttribute(model);
		formAttribute(model);
		return getFormTpl();
	}
	
	@PutMapping("/{id}/edit")
	@Override
	String putEdit(@Validated @ModelAttribute(OB_NAME) RobocopyItem entityFromForm,  final BindingResult bindingResult, @PathVariable(name="id") RobocopyItem entityFromDb,Model model, HttpServletRequest request, RedirectAttributes ras) {
		if (bindingResult.hasErrors()) {
			return getEditNoObName(model);
		}
		if (copyProperties(entityFromForm, entityFromDb)) {
			save(entityFromDb);
		}
        ras.addFlashAttribute("formProcessSuccessed", true);
        return redirectListingUrl(request, KeyValue.of("descriptionId", entityFromDb.getDescriptionId()));
	}
	
	
	@Override
	protected String afterCreate(RobocopyItem entityFromForm, HttpServletRequest request) {
		return "redirect:" + LISTING_PATH + "/" + entityFromForm.getId() + "/edit";
	}

	@Override
	public RobocopyItem newModel() {
		return new RobocopyItem();
	}

	@Override
	protected void formAttribute(Model model) {
		model.addAttribute("crons", reuseableCronDbService.findAll());
	}
	
	@Override
	public List<RobocopyItem> getItemList(HttpServletRequest request) {
		return getDbService().findByDescriptionId(request.getParameter("descriptionId"));
	}
	

	@SuppressWarnings("unchecked")
	@Override
	protected void listExtraAttributes(Model model) {
//		List<RobocopyItem> mis = (List<RobocopyItem>) model.asMap().get(LIST_OB_NAME);
//		List<Server> servers = serverDbService.findByIds(mis.stream().map(RobocopyItem::getServerId).toArray(size -> new Integer[size]));
//		model.addAttribute(ID_ENTITY_MAP, servers.stream().collect(Collectors.toMap(Server::getId, s -> s)));
	}
	
	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}

}
