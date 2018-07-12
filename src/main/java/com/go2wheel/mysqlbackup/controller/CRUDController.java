package com.go2wheel.mysqlbackup.controller;

import java.util.Arrays;
import java.util.List;

import org.atteo.evo.inflector.English;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.service.DbServiceBase;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

public abstract class CRUDController<T extends BaseModel, D extends DbServiceBase<?, T>> extends ControllerBase {
	
	private final Class<T> clazz;
	
	private final String lowerHyphenPlural;
	
	private final D dbService;
	
	private final String mappingUrl;
	
	
	private Converter<String, String> cf = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN);
	
	public static final String LIST_OB_NAME = "listItems";
	public static final String OB_NAME = "singleItem";
	
	public CRUDController(Class<T> clazz,D dbService, String mappingUrl) {
		this.clazz = clazz;
		this.dbService = dbService;
		this.lowerHyphenPlural = English.plural(cf.convert(clazz.getSimpleName()));
		this.mappingUrl = mappingUrl;
		Assert.isTrue(mappingUrl.endsWith(lowerHyphenPlural), "requestmapping url should match classname.");
	}
	
	abstract boolean copyProperties(T entityFromForm, T entityFromDb);
	
	private void commonAttribute(Model model) {
		model.addAttribute("mapping", mappingUrl);
		model.addAttribute("entityName", clazz.getName());
	}
	
	@GetMapping("")
	String getListPage(Model model) {
		model.addAttribute(LIST_OB_NAME, getItemList());
		commonAttribute(model);
		listExtraAttributes(model);
		return getListTpl();
	}
	
	@GetMapping("/create")
	String getCreate(Model model) {
		model.addAttribute(OB_NAME, newModel());
		model.addAttribute("editting", false);
		commonAttribute(model);
		formAttribute(model);
		return getFormTpl();
	}
	

	@PostMapping("/create")
	String postCreate(@Validated @ModelAttribute(OB_NAME) T entityFromForm, final BindingResult bindingResult,Model model, RedirectAttributes ras) {
	    if (bindingResult.hasErrors()) {
	    	commonAttribute(model);
	    	formAttribute(model);
	    	model.addAttribute("editting", false);
	        return getFormTpl();
		}
		try {
			save(entityFromForm);
		} catch (Exception e) {
			if (e instanceof DuplicateKeyException) {
				DuplicateKeyException de = (DuplicateKeyException) e;
				parseDuplicateKeyException(de, ExceptionUtil.parseDuplicateException(de), bindingResult);
			} else {
				bindingResult.addError(new ObjectError(clazz.getSimpleName(), cf.convert(e.getClass().getName())));
			}
	    	commonAttribute(model);
	    	formAttribute(model);
	        return getFormTpl();
		}
	    ras.addFlashAttribute("formProcessSuccessed", true);
	    return "redirect:" + mappingUrl;
	}

	protected void parseDuplicateKeyException(DuplicateKeyException de, String unique, BindingResult bindingResult) {
		bindingResult.addError(new ObjectError(clazz.getSimpleName(), cf.convert(de.getClass().getName())));
	}

	protected abstract void formAttribute(Model model);
	protected abstract void listExtraAttributes(Model model);

	@GetMapping("/{id}/edit")
	String getEdit(@PathVariable(name="id") T entityFromDb, Model model) {
		model.addAttribute(OB_NAME, entityFromDb);
		model.addAttribute("editing", true);
		commonAttribute(model);
		formAttribute(model);
		return getFormTpl();
	}


	@PutMapping("/{id}/edit")
	String putEdit(@Validated @ModelAttribute(OB_NAME) T entityFromForm, @PathVariable(name="id") T entityFromDb,  final BindingResult bindingResult,Model model, RedirectAttributes ras) {
		if (bindingResult.hasErrors()) {
			formAttribute(model);
			return getFormTpl();
		}
		if (copyProperties(entityFromForm, entityFromDb)) {
			save(entityFromDb);
		}
        ras.addFlashAttribute("formProcessSuccessed", true);
	    return "redirect:" + mappingUrl;
	}

	
	@Override
	public List<MainMenuItem> getMenuItems() {
		return Arrays.asList(new MainMenuItem("appmodel", getLowerHyphenPlural(), mappingUrl, getMenuOrder()));
	}
	
	protected int getMenuOrder() {
		return 100000;
	}
	
	public List<T> getItemList() {
		return dbService.findAll();
	}
	
	public void save(T entity) {
		dbService.save(entity);
	}
	
	public abstract T newModel();
	
	public String getFormTpl() {
		return lowerHyphenPlural + "-form";
	}
	
	public String getListTpl() {
		return lowerHyphenPlural + "-list";
	}

	public String getLowerHyphenPlural() {
		return lowerHyphenPlural;
	}

	public D getDbService() {
		return dbService;
	}
}
