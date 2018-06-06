package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.go2wheel.mysqlbackup.yml.YamlInstance;

@Controller
public class SampleController implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World!";
	}

	@ModelAttribute
	public void populateModel(@RequestParam(required = false) String number, Model model) {
		model.addAttribute("number", number);
	}

	@ModelAttribute
	public void populateServerGroup(Model model) throws IOException {
		Path pa = Paths.get("templates", "tplcontext.yml");
		String content = new String(Files.readAllBytes(pa), StandardCharsets.UTF_8);
		Map<String, ?> m = YamlInstance.INSTANCE.yaml.loadAs(content, Map.class);
		model.addAllAttributes(m);
	}

	@GetMapping("/dynamic/{tplName}")
	public String ft(@PathVariable String tplName) {
		return tplName;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@GetMapping("/curdir")
	public ResponseEntity<String> getInfo() {
		return ResponseEntity.ok(Paths.get("").toAbsolutePath().toString());
	}

}
