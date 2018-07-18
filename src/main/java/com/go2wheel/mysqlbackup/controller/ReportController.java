package com.go2wheel.mysqlbackup.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.go2wheel.mysqlbackup.job.MailerJob;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.service.SubscribeDbService;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.value.IdBinder;

@Controller
@RequestMapping("/report")
public class ReportController implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	
	@Autowired
	private MailerJob mailerJob;
	
	@Autowired
	private SubscribeDbService subscribeDbService;
	
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

	@GetMapping("/{tplName}")
	public String ft(@PathVariable String tplName, @RequestParam Subscribe subscribe, Model model) {
		ServerGroupContext sgc = templateContextService.createMailerContext(subscribe);
		model.addAllAttributes(sgc.toMap());
		return tplName;
	}

	@PostMapping("/mail")
	@ResponseBody
	public Map<String, String> sendSubscribeMail(@ModelAttribute IdBinder idBinder, Model model) {
		Subscribe subscribe = subscribeDbService.findById(idBinder.getId());
		ServerGroupContext sgctx = templateContextService.createMailerContext(subscribe);
		mailerJob.mail(sgctx.getUser().getEmail(), subscribe.getTemplate(), sgctx);
		Map<String, String> map = new HashMap<>();
		map.put("email", sgctx.getUser().getEmail());
		return map;
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
