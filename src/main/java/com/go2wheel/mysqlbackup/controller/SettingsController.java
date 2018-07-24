package com.go2wheel.mysqlbackup.controller;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.MyAppSettings.SshConfig;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.service.KeyValueService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.value.KeyValueProperties;


//@Controller
//@RequestMapping(SettingsController.uri)
public class SettingsController  extends ControllerBase {
	
	public static final String uri = "/app/settings";
	public static final String OB_NAME = "sshconfig";
	
	@Autowired
	private KeyValueService keyValueService;
	
	@Autowired
	private MyAppSettings myAppSettings;

	@GetMapping("")
	String getPage(Model model) {
		KeyValueProperties kvp = keyValueService.getPropertiesByPrefix(MyAppSettings.MYAPP_PREFIX, "ssh");
		SshConfig sshConfig = new SshConfig(kvp);
		model.addAttribute(OB_NAME, sshConfig);
		return "settings";
	}

	@PostMapping("")
	String postPage(@Validated @ModelAttribute(OB_NAME) SshConfig sshconfig, final BindingResult bindingResult, RedirectAttributes ras) {
	    if (bindingResult.hasErrors()) {
	        return "settings";
	    }
	    if (!java.nio.file.Files.exists(Paths.get(sshconfig.getSshIdrsa()))) {
	    	bindingResult.addError(new FieldError(OB_NAME, "sshIdrsa", sshconfig.getSshIdrsa(), false, null, null, "文件不存在"));
	    }
	    
	    if (!java.nio.file.Files.exists(Paths.get(sshconfig.getKnownHosts()))) {
	    	bindingResult.addError(new FieldError(OB_NAME, "knownHosts", sshconfig.getKnownHosts(), false, null, null, "文件不存在"));
	    }
	    
	    if (bindingResult.hasErrors()) {
	    	return "settings";
	    }
	    
	    KeyValueProperties kvp = keyValueService.getPropertiesByPrefix(MyAppSettings.MYAPP_PREFIX, "ssh");
	    KeyValue sshIdRsaKv = kvp.getKeyValue(SshConfig.SSH_ID_RSA_KEY).get(); 
	    KeyValue knowsHostsKv = kvp.getKeyValue(SshConfig.KNOWN_HOSTS_KEY).get();
	    
	    if (!sshIdRsaKv.getItemValue().equals(sshconfig.getSshIdrsa())) {
	    	sshIdRsaKv.setItemValue(sshconfig.getSshIdrsa());
	    	keyValueService.save(sshIdRsaKv);
	    	myAppSettings.getSsh().setSshIdrsa(sshconfig.getSshIdrsa());
	    }
	    
	    if (!knowsHostsKv.getItemValue().equals(sshconfig.getKnownHosts())) {
	    	knowsHostsKv.setItemValue(sshconfig.getKnownHosts());
	    	keyValueService.save(sshIdRsaKv);
	    	myAppSettings.getSsh().setKnownHosts(sshconfig.getKnownHosts());
	    }
	    
	    ras.addFlashAttribute("formProcessSuccessed", true);
	    return "redirect:" + uri;
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return Arrays.asList(new MainMenuItem("appmodel", "settings", uri, 200));
	}
}
