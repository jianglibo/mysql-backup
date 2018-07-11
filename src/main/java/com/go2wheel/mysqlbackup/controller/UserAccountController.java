package com.go2wheel.mysqlbackup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.service.UserAccountDbService;


@Controller
@RequestMapping(UserAccountController.MAPPING_PATH)
public class UserAccountController  extends CRUDController<UserAccount, UserAccountDbService> {
	
	public static final String MAPPING_PATH = "/app/user-accounts";
	
	@Autowired
	public UserAccountController(UserAccountDbService dbService) {
		super(UserAccount.class, dbService, MAPPING_PATH);
	}

	@Override
	void copyProperties(UserAccount entityFromForm, UserAccount entityFromDb) {
		entityFromDb.setEmail(entityFromForm.getEmail());
		entityFromDb.setMobile(entityFromForm.getMobile());
		entityFromDb.setName(entityFromForm.getName());
	}

	@Override
	public UserAccount newModel() {
		return new UserAccount();
	}

	@Override
	protected void formAttribute(Model model) {
		
	}

	@Override
	protected void listExtraAttributes(Model model) {
	}


	
//	@Autowired
//	private UserAccountDbService userAccountDbService;
//
//	@GetMapping("")
//	String getPage(Model model) {
//		List<UserAccount> subscribers =  userAccountDbService.findAll();
//		model.addAttribute(LIST_OB_NAME, subscribers);
//		return LIST_TPL;
//	}
//	
//	@GetMapping("/create")
//	String getCreate(Model model) {
//		model.addAttribute(OB_NAME, new UserAccount());
//		return FORM_TPL;
//	}
//	
//	@PostMapping("/create")
//	String postCreate(@Validated @ModelAttribute(OB_NAME) UserAccount subscriber, final BindingResult bindingResult,Model model, RedirectAttributes ras) {
//	    if (bindingResult.hasErrors()) {
//	        return FORM_TPL;
//		}
//		userAccountDbService.save(subscriber);
//	    ras.addFlashAttribute("formProcessSuccessed", true);
//	    return "redirect:" + uri;
//	}
//
//	@GetMapping("/{id}/edit")
//	String getEdit(@PathVariable(name="id") UserAccount subscriber, Model model) {
//		model.addAttribute(OB_NAME, subscriber);
//		model.addAttribute("editing", true);
//		return FORM_TPL;
//	}
//
//	@PutMapping("/{id}/edit")
//	String putEdit(@Validated @ModelAttribute(OB_NAME) UserAccount subscriberUpdated, @PathVariable(name="id") UserAccount subscriberOrigin,  final BindingResult bindingResult,Model model, RedirectAttributes ras) {
//		if (bindingResult.hasErrors()) {
//	        return FORM_TPL;
//		}
//		subscriberOrigin.setEmail(subscriberUpdated.getEmail());
//		subscriberOrigin.setMobile(subscriberUpdated.getMobile());
//		subscriberOrigin.setName(subscriberUpdated.getName());
//		userAccountDbService.save(subscriberOrigin);
//        ras.addFlashAttribute("formProcessSuccessed", true);
//	    return "redirect:" + uri;
//	}
//
//	@Override
//	public List<MainMenuItem> getMenuItems() {
//		return Arrays.asList(new MainMenuItem("appmodel", "subscribers", uri, 200));
//	}
}
