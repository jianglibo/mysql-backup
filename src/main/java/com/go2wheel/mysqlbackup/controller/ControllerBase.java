package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.dbservice.GlobalStore;
import com.go2wheel.mysqlbackup.ui.MainMenuGroups;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.util.TplUtil;

public abstract class ControllerBase implements ApplicationContextAware {
	
	protected ApplicationContext applicationContext;
	
	public static final String LIST_OB_NAME = "listItems";
	public static final String ID_ENTITY_MAP = "idEntityMap";
	public static final String OB_NAME = "singleItem";
	
	public static final String ERROR_MESSAGE_KEY = "errorMessage";
	
	@Autowired
	protected SettingsInDb settingsInDb;
	
	@Autowired
	protected GlobalStore globalStore;
	
	@Autowired
	private MainMenuGroups menuGroups;
	
	@Autowired
	protected MessageSource messageSource;
	
	private final String listingUrl;
	
	public ControllerBase(String listingUrl) {
		this.listingUrl = listingUrl;
	}

	@ModelAttribute
	public void populateMainMenu(Model model, HttpServletRequest request) {
		List<MainMenuItem> items = menuGroups.clone().prepare(request.getRequestURI()).getMenuItems();
		model.addAttribute("menus", items);
		model.addAttribute("listingUrl", listingUrl);
		model.addAttribute("tplUtil", new TplUtil());
	}
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public abstract MainMenuItem getMenuItem();
	
	public String getListingUrl() {
		return listingUrl;
	}
	
	public String getI18nedMessage(String msgkey, Object...substitutes) {
		return messageSource.getMessage(msgkey, substitutes,
				LocaleContextHolder.getLocale());

	}
	
	
	protected String getTplName(String full) {
		int p = full.lastIndexOf('/');
		if (p == -1) {
			return full;
		} else {
			return full.substring(p);
		}
	}

}
