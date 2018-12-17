package com.go2wheel.mysqlbackup.mail;

import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.MailBaseFort;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.value.Subscribe;

public class TestMailer extends MailBaseFort {
	
	@Autowired
	private Mailer mailer;
	
	@Autowired
	private TemplateContextService templateContextService;
	
	
	@Test
	public void tmail() throws Exception {
		Path[] pss = getPathes();
		userGroupLoader.clearAll();
		configFileLoader.clearCache();
		configFileLoader.loadAll(psappconfig, true);
		userGroupLoader.loadAll(pss[0], pss[1], pss[2], pss[3]);
		
		Subscribe subscribe = userGroupLoader.getAllSubscribes().get(0);
		
		String mailContent = mailer.renderTemplate("ctx.html", templateContextService.createMailerContext(subscribe));
		System.out.println(mailContent);
		mailer.sendMail(subscribe, "jianglibo@hotmail.com", "ctx.html", templateContextService.createMailerContext(subscribe));
	}

}
