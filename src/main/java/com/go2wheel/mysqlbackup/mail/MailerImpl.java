package com.go2wheel.mysqlbackup.mail;

import com.go2wheel.mysqlbackup.util.ChromePdfWriter;
import com.go2wheel.mysqlbackup.value.Subscribe;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailerImpl implements Mailer {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private JavaMailSender javaMailSender;

  @Autowired
  private EmailViewRender emailViewRender;

  @Autowired
  private MailProperties mailProperties;

  @Autowired
  private ChromePdfWriter pdfWriter;

  @Value("${server.port}")
  private int port;

  public void sendMail(Subscribe subscribe, String email, String template, ServerGroupContext sgctx)
      throws MessagingException, UnsupportedEncodingException {
    final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
    // true = multipart
    final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
    message.setFrom(mailProperties.getUsername());
    message.setTo(email);
    String htmlContent = emailViewRender.render(template, sgctx);
    String title = sgctx.getServerGroup().getName() + "的备份和工况报表";
    message.setSubject(MimeUtility.encodeText(title, "UTF-8", "B"));
    message.setText(htmlContent, true); // true = isHtml
    String url = String.format("http://localhost:%s/app/report/html/%s", port, subscribe.getId());
    logger.info("start visiting {}", url);
    Path pdf = pdfWriter.writePdf(url);
    if (pdf != null) {
      FileSystemResource file = new FileSystemResource(pdf.toFile());
      message.addAttachment("report.pdf", file);
    }
    this.javaMailSender.send(mimeMessage);
  }

  @Override
  public String renderTemplate(String template, ServerGroupContext rc) {
    return emailViewRender.render(template, rc);
  }

  @Override
  public void sendMailPlainText(String subject, String content, String email)
      throws MessagingException, UnsupportedEncodingException {
    final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
    final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
    message.setFrom(mailProperties.getUsername());
    message.setTo(email);
    message.setSubject(MimeUtility.encodeText(subject));
    message.setText(content, false); // true = isHtml
    this.javaMailSender.send(mimeMessage);
  }
}
