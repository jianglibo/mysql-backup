package com.go2wheel.mysqlbackup.controller;

import java.util.Arrays;
import java.util.List;

import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.service.UserAccountDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping(UserAccountController.uri)
public class UserAccountController  extends ControllerBase {
	
	public static final String uri = "/app/subscribers";
	public static final String LIST_OB_NAME = "subscribers";
	public static final String OB_NAME = "subscriber";
	
	private static final String FORM_TPL = "subscriber-form";
	private static final String LIST_TPL = "subscribers";
	
	@Autowired
	private UserAccountDbService userAccountDbService;

	@GetMapping("")
	String getPage(Model model) {
		List<UserAccount> subscribers =  userAccountDbService.findAll();
		model.addAttribute(LIST_OB_NAME, subscribers);
		return LIST_TPL;
	}
	
	@GetMapping("/create")
	String getCreate(Model model) {
		model.addAttribute(OB_NAME, new UserAccount());
		return FORM_TPL;
	}
	
	@PostMapping("/create")
	String postCreate(@Validated @ModelAttribute(OB_NAME) UserAccount subscriber, final BindingResult bindingResult,Model model, RedirectAttributes ras) {
	    if (bindingResult.hasErrors()) {
	        return FORM_TPL;
		}
		userAccountDbService.save(subscriber);
	    ras.addFlashAttribute("formProcessSuccessed", true);
	    return "redirect:" + uri;
	}

	@GetMapping("/{id}/edit")
	String getEdit(@PathVariable(name="id") UserAccount subscriber, Model model) {
		model.addAttribute(OB_NAME, subscriber);
		model.addAttribute("editing", true);
		return FORM_TPL;
	}

	@PutMapping("/{id}/edit")
	String putEdit(@Validated @ModelAttribute(OB_NAME) UserAccount subscriberUpdated, @PathVariable(name="id") UserAccount subscriberOrigin,  final BindingResult bindingResult,Model model, RedirectAttributes ras) {
		if (bindingResult.hasErrors()) {
	        return FORM_TPL;
		}
		subscriberOrigin.setEmail(subscriberUpdated.getEmail());
		subscriberOrigin.setMobile(subscriberUpdated.getMobile());
		subscriberOrigin.setName(subscriberUpdated.getName());
		userAccountDbService.save(subscriberOrigin);
        ras.addFlashAttribute("formProcessSuccessed", true);
	    return "redirect:" + uri;
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return Arrays.asList(new MainMenuItem("appmodel", "subscribers", uri, 200));
	}
}
