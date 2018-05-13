package com.go2wheel.mysqlbackup.mail;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.context.Context;

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

		// helper.setTo(to);
		// helper.setSubject(subject);
		// helper.setText(text);
		//
		// FileSystemResource file
		// = new FileSystemResource(new File(pathToAttachment));
		// helper.addAttachment("Invoice", file);

		javaMailSender.send(message);
	}

	public void sendMailWithInline(final String recipientName, final String recipientEmail,
			final String imageResourceName, final byte[] imageBytes, final String imageContentType, final Locale locale)
			throws MessagingException {

//		// Prepare the evaluation context
//		final Context ctx = new Context(locale);
//		ctx.setVariable("name", recipientName);
//		ctx.setVariable("subscriptionDate", new Date());
//		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));
//		ctx.setVariable("imageResourceName", imageResourceName); // so that we can reference it from HTML
//
//		// Prepare message using a Spring helper
//		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
//		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
//		message.setSubject("Example HTML email with inline image");
//		message.setFrom("thymeleaf@example.com");
//		message.setTo(recipientEmail);
//
//		// Create the HTML body using Thymeleaf
//		final String htmlContent = this.templateEngine.process("email-inlineimage.html", ctx);
//		message.setText(htmlContent, true); // true = isHtml
//
//		// Add the inline image, referenced from the HTML code as
//		// "cid:${imageResourceName}"
//		final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
//		message.addInline(imageResourceName, imageSource, imageContentType);
//
//		// Send mail
//		this.mailSender.send(mimeMessage);

	}
}
