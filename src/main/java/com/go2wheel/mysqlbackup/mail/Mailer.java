package com.go2wheel.mysqlbackup.mail;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Service
public class Mailer {

	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	private TemplateEngine emailTemplateEngine;

	
	public void sendMailWithInline(final String recipientName, final String recipientEmail,
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
