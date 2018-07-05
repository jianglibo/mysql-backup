package com.go2wheel.mysqlbackup.mail;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Locale;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.ViewResolverComposite;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.AbstractContext;
import org.thymeleaf.exceptions.TemplateInputException;

import com.go2wheel.mysqlbackup.SpringBaseFort;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
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

	@Test(expected=TemplateInputException.class)
	public void tThymeleafTplNoExt() {
		templateEngine.process("content", new AbstractContext() {});
	}
	
	@Test
	public void tThymeleafTplWithExt() {
		templateEngine.process("a/a.html", new AbstractContext() {});
	}
}
