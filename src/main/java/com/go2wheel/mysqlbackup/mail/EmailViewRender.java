package com.go2wheel.mysqlbackup.mail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import com.go2wheel.mysqlbackup.util.ExceptionUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Service
public class EmailViewRender {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private TemplateEngine templateEngine;
	
	@Autowired
	private Configuration freemarkerConfiguration;
	

	public String render(String template, ServerGroupContext sgc) {
		if (template.endsWith(".ftl")) {
			return processFreemarker(template, sgc);
		}
		return processThymeleaf(template, sgc);
	}


	private String processThymeleaf(String template, ServerGroupContext sgc) {
		IContext ctx = new Context(Locale.getDefault(), sgc.toMap());
		return templateEngine.process(template, ctx);
	}


	private String processFreemarker(String template, ServerGroupContext sgc) {
		try {
			Template tp = freemarkerConfiguration.getTemplate(template);
			StringWriter sw = new StringWriter();
			tp.process(sgc.toMap(), sw);
			return sw.toString();
		} catch (IOException e) {
			ExceptionUtil.logErrorException(logger, e);
		} catch (TemplateException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
		return "";
	}

}
