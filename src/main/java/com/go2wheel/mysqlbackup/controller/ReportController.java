package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.go2wheel.mysqlbackup.AppEventListenerBean;
import com.go2wheel.mysqlbackup.JavaMailSendPropertiesOverrider;
import com.go2wheel.mysqlbackup.job.MailerJob;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.service.ConfigFileLoader;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.util.ChromePdfWriter;
import com.go2wheel.mysqlbackup.value.Subscribe;
import com.google.common.io.ByteStreams;

@Controller
@RequestMapping("/app/report")
public class ReportController {

	@Autowired
	private MailProperties mailProperties;
	
	@Autowired
	private MailerJob mailerJob;
	
	@Autowired
	private UserGroupLoader userGroupLoader;
	
	@Autowired
	private ConfigFileLoader configFileLoader;
	
	@Autowired
	private AppEventListenerBean appEventListenerBean;
	
	@Autowired
	private TemplateContextService templateContextService;
	
	@Autowired
	private ChromePdfWriter pdfWriter;

	@GetMapping("/{tplName}")
	public String ft(@PathVariable String tplName, @RequestParam String subscribe, Model model) throws Exception {
		Subscribe subscribeOb = userGroupLoader.getSubscribeById(subscribe);
		if (subscribeOb == null) {
			loadUserGroups(false);
			subscribeOb = userGroupLoader.getSubscribeById(subscribe);
		}
		ServerGroupContext sgc = templateContextService.createMailerContext(subscribeOb);
		model.addAllAttributes(sgc.toMap());
		return tplName;
	}
	
	@GetMapping("/html/{subscribe}")
	public String ftPost(@PathVariable String subscribe, Model model) throws Exception {
		Subscribe subscribeOb = userGroupLoader.getSubscribeById(subscribe);
		if (subscribeOb == null) {
			loadUserGroups(false);
			subscribeOb = userGroupLoader.getSubscribeById(subscribe);
		}
		ServerGroupContext sgc = templateContextService.createMailerContext(subscribeOb);
		model.addAllAttributes(sgc.toMap());
		return subscribeOb.getTemplate();
	}
	
	@GetMapping("/pdf/{subscribe}")
	@ResponseBody
	public ResponseEntity<StreamingResponseBody> pdfPost(@PathVariable String subscribe, Model model, HttpServletRequest request) throws Exception {
		Subscribe subscribeOb = userGroupLoader.getSubscribeById(subscribe);
		if (subscribeOb == null) {
			loadUserGroups(false);
			subscribeOb = userGroupLoader.getSubscribeById(subscribe);
		}
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		String urlPath = request.getRequestURI();
		String htmlUrl = ucb.replacePath(urlPath.replaceFirst("pdf", "html")).build().toUriString();
		Path pdfFile = pdfWriter.writePdf(htmlUrl);
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.parseMediaType("application/pdf"));
	    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	    return new ResponseEntity<>(new StreamingResponseBody() {
			@Override
			public void writeTo(OutputStream outputStream) throws IOException {
				try(InputStream is = Files.newInputStream(pdfFile)) {
					ByteStreams.copy(is, outputStream);
					outputStream.flush();
				}
			}
		}, headers, HttpStatus.OK);

	}
	
	@GetMapping("/loaddata")
	@ResponseBody
	public String loadUserGroups(@RequestParam(required=false) boolean schedule) throws Exception {
		appEventListenerBean.loadData(null, schedule, true);
		return "OK";
	}
	
	@GetMapping("/mailsettings")
	@ResponseBody
	public Map<String, String> mailSettings() {
		Map<String, String> map = new HashMap<>();
		map.put(JavaMailSendPropertiesOverrider.SPRING_MAIL_HOST, mailProperties.getHost());
		map.put(JavaMailSendPropertiesOverrider.SPRING_MAIL_USERNAME, mailProperties.getUsername());
		return map;
	}

	@PostMapping(path = "/mail")
	@ResponseBody
	public Map<String, String> sendSubscribeMail(@RequestParam String id, Model model) throws UnsupportedEncodingException, MessagingException, ExecutionException {
		Subscribe subscribe = userGroupLoader.getSubscribeById(id);
		ServerGroupContext sgctx = templateContextService.createMailerContext(subscribe);
		mailerJob.mail(subscribe, sgctx.getUser().getEmail(), subscribe.getTemplate(), sgctx);
		Map<String, String> map = new HashMap<>();
		map.put("email", sgctx.getUser().getEmail());
		return map;
	}

}
