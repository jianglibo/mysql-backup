package com.go2wheel.mysqlbackup.controller;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ViewResolver;

@Controller
public class SampleController implements ApplicationContextAware {
	
	@Value("${spring.thymeleaf.prefix}")
	private String prefix;
	
	@Value("${spring.thymeleaf.suffix}")
	private String suffix;
	
	private ApplicationContext applicationContext;
	
    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World!";
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("prefix: " + prefix);
    	sb.append("\n");
    	sb.append("postfix: " + suffix);
    	sb.append("\n");
    	
    	Map<String, ViewResolver> vrs = applicationContext.getBeansOfType(ViewResolver.class);
    	
        return ResponseEntity.ok(sb.toString());
    }

    
    @GetMapping("/content")
    public ModelAndView getContent(@RequestParam(required=false) String tpl) {
        ModelAndView mav = new ModelAndView();
        if (tpl == null) {
        	mav.setViewName("content");
        } else {
        	mav.setViewName(tpl);
        }
        return mav;
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
