package com.go2wheel.mysqlbackup.mail;

import javax.mail.MessagingException;

public interface Mailer {
	
	public static final String THYMELEAF = "THYMELEAF";
	public static final String FREEMARKER = "FREEMARKER";
	
	void sendMailWithInline(ServerGroupContext rc) throws MessagingException;
}