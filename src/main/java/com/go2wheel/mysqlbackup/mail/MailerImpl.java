package com.go2wheel.mysqlbackup.mail;

import java.io.File;
import java.nio.file.Path;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.util.ChromePDFWriter;

@Service
public class MailerImpl implements Mailer {

	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	private EmailViewRender emailViewRender;
	
	@Value("${spring.mail.username}")
	private String mailFrom;
	
	@Autowired
	private ChromePDFWriter pdfWriter;
	
//	public void sendMailWithInline(final String recipientName, final String recipientEmail,
//			final String imageResourceName, final byte[] imageBytes, final String imageContentType, final Locale locale)
//			throws MessagingException {
//
//		// Prepare the evaluation context
//		final Context ctx = new Context(locale);
//		ctx.setVariable("name", recipientName);
//		ctx.setVariable("subscriptionDate", new Date());
//		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));
//		ctx.setVariable("imageResourceName", imageResourceName); // so that we can reference it from HTML
//
//		// Prepare message using a Spring helper
//		final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
//		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
//		message.setSubject("Example HTML email with inline image");
//		message.setFrom("391772322@qq.com");
//		message.setTo(recipientEmail);
//
//		// Create the HTML body using Thymeleaf
//		Set<IMessageResolver> mresolvers = this.emailTemplateEngine.getMessageResolvers();
//		Set<ITemplateResolver> tresolvers = this.emailTemplateEngine.getTemplateResolvers();
//		final String htmlContent = emailViewRender.render("", sgc)
//		message.setText(htmlContent, true); // true = isHtml
//
//		// Add the inline image, referenced from the HTML code as
//		// "cid:${imageResourceName}"
//		final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
//		message.addInline(imageResourceName, imageSource, imageContentType);
//
//		// Send mail
//		 this.javaMailSender.send(mimeMessage);
//
//	}

	public void sendMailWithInline(Subscribe subscribe,
			String email, String template, ServerGroupContext sgctx) throws MessagingException {
		final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
		mimeMessage.setSubject("服务器备份报表。", "UTF-8");
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
		message.setFrom(mailFrom);
		message.setTo(email);
		String htmlContent = emailViewRender.render(template, sgctx);
		message.setText(htmlContent, true); // true = isHtml
		
		Path pdf = pdfWriter.writePdf("http://localhost:8080/app/report/html/" + subscribe.getId());
		if (pdf != null) {
		    FileSystemResource file 
		      = new FileSystemResource(pdf.toFile());
		    message.addAttachment("report.pdf", file);
		}
		this.javaMailSender.send(mimeMessage);
	}

	@Override
	public String renderTemplate(String template, ServerGroupContext rc) {
		return emailViewRender.render(template, rc);
	}

}
