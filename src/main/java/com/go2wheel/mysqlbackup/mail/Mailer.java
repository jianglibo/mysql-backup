package com.go2wheel.mysqlbackup.mail;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import com.go2wheel.mysqlbackup.model.Subscribe;

public interface Mailer {
	
	public static final String DEFAULT_EMIAL_TPL = "mail-template.html";
	
	public static final String THYMELEAF = "THYMELEAF";
	public static final String FREEMARKER = "FREEMARKER";
	
	void sendMailWithInline(Subscribe subscribe,
			String email, String template, ServerGroupContext rc) throws MessagingException, UnsupportedEncodingException;
	
	String renderTemplate(String template, ServerGroupContext rc);
}
