package com.go2wheel.mysqlbackup.mail;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import com.go2wheel.mysqlbackup.SpringBaseFort;

public class TestMailIgnored extends SpringBaseFort {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private TemplateEngine emailTemplateEngine;

	private String userTo = "jianglibo@hotmail.com";
	private String userFrom = "jlbfine@qq.com";
	private String subject = "Test subject";
	private String textMail = "Text subject mail";


	private SimpleMailMessage composeEmailMessage() {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(userTo);
		mailMessage.setReplyTo(userFrom);
		mailMessage.setFrom(userFrom);
		mailMessage.setSubject(subject);
		mailMessage.setText(textMail);
		return mailMessage;
	}

//	public void tMimeMessage() throws MessagingException {
//		MimeMessage message = javaMailSender.createMimeMessage();

//		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		// helper.setTo(to);
		// helper.setSubject(subject);
		// helper.setText(text);
		//
		// FileSystemResource file
		// = new FileSystemResource(new File(pathToAttachment));
		// helper.addAttachment("Invoice", file);

//		javaMailSender.send(message);
//	}

	@Test
	public void tt() {
		assertTrue(true);
	}
//	@Test
	public void tThyme() throws MessagingException, IOException {
//		final Context ctx = new Context(Locale.CHINESE);
//		ctx.setVariable("name", "jlb");
//		ctx.setVariable("subscriptionDate", new Date());
//		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));
//		ctx.setVariable("imageResourceName", imageResourceName); // so that we can reference it from HTML
//
//		final String htmlContent = this.emailTemplateEngine.process("html/email-inlineimage.html", ctx);
		Resource resource = 
				applicationContext.getResource("classpath:mail/runbat.PNG");
		
		try (InputStream is = resource.getInputStream()) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}

			buffer.flush();
//			MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

			sendMailWithInline("jlb", "jianglibo@hotmail.com", "thymeleaflogo", buffer.toByteArray(), "image/png", Locale.CHINESE);
		}
		
	}

	private void sendMailWithInline(final String recipientName, final String recipientEmail,
			final String imageResourceName, final byte[] imageBytes, final String imageContentType, final Locale locale)
			throws MessagingException {

		// Prepare the evaluation context
		final Context ctx = new Context(locale);
		ctx.setVariable("name", recipientName);
		ctx.setVariable("subscriptionDate", new Date());
		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));
		ctx.setVariable("imageResourceName", imageResourceName); // so that we can reference it from HTML

		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
		message.setSubject("Example HTML email with inline image");
		message.setFrom("391772322@qq.com");
		message.setTo(recipientEmail);

		// Create the HTML body using Thymeleaf
		Set<IMessageResolver> mresolvers = this.emailTemplateEngine.getMessageResolvers();
		Set<ITemplateResolver> tresolvers = this.emailTemplateEngine.getTemplateResolvers();
		final String htmlContent = this.emailTemplateEngine.process("html/email-inlineimage.html", ctx);
		message.setText(htmlContent, true); // true = isHtml

		// Add the inline image, referenced from the HTML code as
		// "cid:${imageResourceName}"
		final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
		message.addInline(imageResourceName, imageSource, imageContentType);

		// Send mail
		 this.javaMailSender.send(mimeMessage);

	}
}
