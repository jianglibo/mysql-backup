package com.go2wheel.mysqlbackup.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import freemarker.template.Configuration;

@Service
public class EmailViewRender {
	
	@Autowired
	private TemplateEngine templateEngine;
	
	@Autowired
	private Configuration freemarkerConfiguration;
	
	
	
	

}
