package com.go2wheel.mysqlbackup.mail;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.AbstractContext;
import org.thymeleaf.context.IContext;

import com.go2wheel.mysqlbackup.SpringBaseFort;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class TestTemplates extends SpringBaseFort {
	
	@Autowired
	private Configuration freemarkerConfiguration;
	
	@Autowired
	private TemplateEngine templateEngine;
	
	@Test(expected = TemplateNotFoundException.class)
	public void tFreemarkerTplNoExt() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
		freemarkerConfiguration.getTemplate("content");
	}
	
	@Test
	public void tFreemarkerTplWithExt() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
		Template tp = freemarkerConfiguration.getTemplate("content.html");
		assertNotNull(tp);
	}
	
	
	@Test()
	public void tThymeleafTplNoExt() {
		templateEngine.process("content", new AbstractContext() {});
	}
	
	@Test
	public void tThymeleafTplWithExt() {
		templateEngine.process("content.html", new AbstractContext() {});
	}
	
//	@Test
//	public void tFreemarkerTpl() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
//		Template tp = freemarkerConfiguration.getTemplate("content");
//		StringWriter sw = new StringWriter();
//		tp.process(new HashMap<>(), sw);
//		
//		String r= sw.toString();
//	}


}
