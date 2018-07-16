package com.go2wheel.mysqlbackup.controller;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.service.TemplateContextService;

@Controller
public class ReportController implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	
	@Autowired
	private TemplateContextService templateContextService;


//	@ModelAttribute
//	public void populateServerGroup(@RequestParam(required = false) String ctxFile, Model model) throws IOException {
//		if (ctxFile == null) {
//			ctxFile = "tplcontext.yml";
//		}
//		Path pa = Paths.get("templates", ctxFile);
//		String content = new String(Files.readAllBytes(pa), StandardCharsets.UTF_8);
//		ServerGroupContext m = YamlInstance.INSTANCE.yaml.loadAs(content, ServerGroupContext.class);
//		model.addAllAttributes(m.toMap());
//	}

	@GetMapping("/report/{tplName}")
	public String ft(@PathVariable String tplName, @RequestParam Subscribe subscribe, Model model) {
		ServerGroupContext sgc = templateContextService.createMailerContext(subscribe);
		model.addAllAttributes(sgc.toMap());
		return tplName;
	}
	
	
//	@GetMapping("/createctx/{userServerGrpId}")
//	public ResponseEntity<String> createctx(@PathVariable int userServerGrpId) {
//		templateContextService.createMailerContext(userServerGrpId);
//		return ResponseEntity.ok("done");
//	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

//	@GetMapping("/curdir")
//	public ResponseEntity<String> getInfo() {
//		return ResponseEntity.ok(Paths.get("").toAbsolutePath().toString());
//	}

}
