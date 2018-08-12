package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpSession;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.util.FutureUtil;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

@Controller
public class SampleController implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	
	@Autowired
	private TemplateContextService templateContextService;
	
	@Autowired
	private GlobalStore globalStore;


	@ModelAttribute
	public void populateServerGroup(@RequestParam(required = false) String ctxFile, Model model) throws IOException {
		if (ctxFile == null) {
			ctxFile = "tplcontext.yml";
		}
		Path pa = Paths.get("templates", ctxFile);
		String content = new String(Files.readAllBytes(pa), StandardCharsets.UTF_8);
		ServerGroupContext m = YamlInstance.INSTANCE.yaml.loadAs(content, ServerGroupContext.class);
		model.addAllAttributes(m.toMap());
	}

	@GetMapping("/dynamic/{tplName}")
	public String ft(@PathVariable String tplName) {
		return tplName;
	}
	
	@GetMapping("/dynamic/{dir}/{tplName}")
	public String ftmore(@PathVariable String dir, @PathVariable String tplName) {
		return dir + "/" + tplName;
	}
	
	@GetMapping("/createctx/{userServerGrpId}")
	public ResponseEntity<String> createctx(@PathVariable int userServerGrpId) {
		templateContextService.createMailerContext(userServerGrpId);
		return ResponseEntity.ok("done");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@GetMapping("/curdir")
	public ResponseEntity<String> getInfo() {
		return ResponseEntity.ok(Paths.get("").toAbsolutePath().toString());
	}
	
//	@GetMapping("/quotes")
//	@ResponseBody
//	public DeferredResult<String> quotes(HttpSession session, @RequestParam long timeout) {
//		DeferredResult<String> deferredResult = new DeferredResult<String>(timeout, "timeout");
//		globalStore.saveObject("test", "http", Gobject.newGobject("name", deferredResult));
//		return deferredResult;
//	}
//	
//	@GetMapping("/quotesfill")
//	@ResponseBody
//	public String quotesfill(HttpSession session) {
//		DeferredResult<String> deferredResult =  globalStore.removeObject("test", "http").as(DeferredResult.class);
//		deferredResult.setResult("yes");
//		return "yes";
//	}
	
	
	@GetMapping("/quotesfuture")
	@ResponseBody
	public CompletableFuture<AsyncTaskValue> quotesfuture(HttpSession session, @RequestParam long timeout) {
		CompletableFuture<AsyncTaskValue> ftw;
		CompletableFuture<AsyncTaskValue> ft = new CompletableFuture<AsyncTaskValue>();
		ftw = FutureUtil.within(ft, Duration.ofMillis(timeout)).exceptionally(throwable -> {
			return new AsyncTaskValue(1L, "timeout");
		});
		Long aid = 1L;
		
		SavedFuture sf = SavedFuture.newSavedFuture(aid, "futrue", ft);
		globalStore.saveFuture("test", sf);
		return ftw;
	}
	
	@GetMapping("/quotesfuturefill")
	@ResponseBody
	public String quotesfillfuture(HttpSession session) {
		SavedFuture  future =  globalStore.removeFuture(1L);
		if (future != null) {
			future.getCf().complete(new AsyncTaskValue(1L, "future"));
			return "yes";
		}
		return "no";
	}

}
