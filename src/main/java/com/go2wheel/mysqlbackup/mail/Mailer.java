package com.go2wheel.mysqlbackup.mail;

import javax.mail.MessagingException;

public interface Mailer {
	
	public static final String DEFAULT_EMIAL_TPL = "mail-template.html";
	
	public static final String THYMELEAF = "THYMELEAF";
	public static final String FREEMARKER = "FREEMARKER";
	
	void sendMailWithInline(String template, ServerGroupContext rc) throws MessagingException;
}
