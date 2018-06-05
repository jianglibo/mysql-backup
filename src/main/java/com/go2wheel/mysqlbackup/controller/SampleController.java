package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.nio.file.Paths;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Controller
public class SampleController implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
	@Autowired
	private Configuration freemarkerConfiguration;
	
    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World!";
    }
    
    @GetMapping("/curdir")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok(Paths.get("").toAbsolutePath().toString());
    }
    
    @ModelAttribute
    public void populateModel(@RequestParam(required=false) String number, Model model) {
        model.addAttribute("number", number);
    }
    
    @GetMapping("/freemarker/{tplName}.ftl")
    public String ft(@PathVariable String tplName) {
    	return "freemarker/" + tplName;
    }
    
    @GetMapping("/thymeleaf/{tplName}.html")
    public String tl(@PathVariable String tplName) {
    	return "thymeleaf/" + tplName;
    }
    
    
    @GetMapping("/{name:[a-z-]+}-{version:\\d\\.\\d\\.\\d}{ext:\\.[a-z]+}")
    public void handle(@PathVariable String version, @PathVariable String ext) {
    }
    
    @GetMapping("/t")
    public ModelAndView getContent(@RequestParam(required=false) String tpl) {
    	
        ModelAndView mav = new ModelAndView();
        if (tpl == null) {
        	tpl = "t.ftl";
        }
        mav.setViewName(tpl);
        
        try {
			Template t = freemarkerConfiguration.getTemplate(tpl);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return mav;
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
