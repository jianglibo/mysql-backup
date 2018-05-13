package com.go2wheel.mysqlbackup.mail;

import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestMail {
	
	@Autowired
	private JavaMailSender javaMailSender;
	
    private String userTo = "jianglibo@hotmail.com";
    private String userFrom = "jlbfine@qq.com";
    private String subject = "Test subject";
    private String textMail = "Text subject mail";

	@Test
	public void tsend() {
        SimpleMailMessage message = composeEmailMessage();
        javaMailSender.send(message);
	}

	private SimpleMailMessage composeEmailMessage() {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userTo);
        mailMessage.setReplyTo(userFrom);
        mailMessage.setFrom(userFrom);
        mailMessage.setSubject(subject);
        mailMessage.setText(textMail);
        return mailMessage;
	}
	
	public void tMimeMessage() throws MessagingException {
	    MimeMessage message = javaMailSender.createMimeMessage();
	      
	    MimeMessageHelper helper = new MimeMessageHelper(message, true);
	     
//	    helper.setTo(to);
//	    helper.setSubject(subject);
//	    helper.setText(text);
//	         
//	    FileSystemResource file 
//	      = new FileSystemResource(new File(pathToAttachment));
//	    helper.addAttachment("Invoice", file);
	 
	    javaMailSender.send(message);
	}
	
}
