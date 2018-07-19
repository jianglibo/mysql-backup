package com.go2wheel.mysqlbackup.mail;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
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
	
	@Autowired
	private MailProperties mailProperties;
	
	@Autowired
	private ChromePDFWriter pdfWriter;

	public void sendMailWithInline(Subscribe subscribe,
			String email, String template, ServerGroupContext sgctx) throws MessagingException, UnsupportedEncodingException {
		final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
		message.setFrom(mailProperties.getUsername());
		message.setTo(email);
		String htmlContent = emailViewRender.render(template, sgctx);
		message.setSubject(MimeUtility.encodeText(sgctx.getServerGroup().getEname() + "的备份和工况报表", "UTF-8", "B"));
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
