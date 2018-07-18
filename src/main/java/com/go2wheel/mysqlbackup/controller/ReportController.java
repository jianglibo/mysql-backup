package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.go2wheel.mysqlbackup.job.MailerJob;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.service.SubscribeDbService;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.util.ChromePDFWriter;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.IdBinder;
import com.google.common.io.ByteStreams;

@Controller
@RequestMapping("/app/report")
public class ReportController implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MailerJob mailerJob;
	
	@Autowired
	private SubscribeDbService subscribeDbService;
	
	@Autowired
	private TemplateContextService templateContextService;
	
	@Autowired
	private ChromePDFWriter pdfWriter;
	
	@ExceptionHandler
	public ResponseEntity<String> handle(Exception ex) {
		ExceptionUtil.logErrorException(logger, ex);
		return ResponseEntity.ok("OK");
	}


//	@ModelAttribute
//	public void populateServerGroup(@RequestParam(required = false) String ctxFile, Model model) throws IOException {
//		if (ctxFile == null) {
//			ctxFile = "tplcontext.yml";
//		}
//		Path pa = Paths.get("templates", ctxFile);
//		String content = new String(Files.readAllBytes(pa), StandardCharsets.UTF_8);
//		ServerGroupContext m = YamlInstance.INSTANCE.yaml.loadAs(content, ServerGroupContext.class);
//		model.addAllAttributes(m.toMap());
//	}

	@GetMapping("/{tplName}")
	public String ft(@PathVariable String tplName, @RequestParam Subscribe subscribe, Model model) {
		ServerGroupContext sgc = templateContextService.createMailerContext(subscribe);
		model.addAllAttributes(sgc.toMap());
		return tplName;
	}
	
	@GetMapping("/html/{subscribe}")
	public String ftPost(@PathVariable Subscribe subscribe, Model model) {
		ServerGroupContext sgc = templateContextService.createMailerContext(subscribe);
		model.addAllAttributes(sgc.toMap());
		return subscribe.getTemplate();
	}
	
	@GetMapping("/pdf/{subscribe}")
	@ResponseBody
	public ResponseEntity<StreamingResponseBody> pdfPost(@PathVariable Subscribe subscribe, Model model, HttpServletRequest request) throws IOException {
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

	@PostMapping("/mail")
	@ResponseBody
	public Map<String, String> sendSubscribeMail(@ModelAttribute IdBinder idBinder, Model model) {
		Subscribe subscribe = subscribeDbService.findById(idBinder.getId());
		ServerGroupContext sgctx = templateContextService.createMailerContext(subscribe);
		mailerJob.mail(subscribe, sgctx.getUser().getEmail(), subscribe.getTemplate(), sgctx);
		Map<String, String> map = new HashMap<>();
		map.put("email", sgctx.getUser().getEmail());
		return map;
	}
	
	
//	@GetMapping("/createctx/{userServerGrpId}")
//	public ResponseEntity<String> createctx(@PathVariable int userServerGrpId) {
//		templateContextService.createMailerContext(userServerGrpId);
//		return ResponseEntity.ok("done");
//	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

//	@GetMapping("/curdir")
//	public ResponseEntity<String> getInfo() {
//		return ResponseEntity.ok(Paths.get("").toAbsolutePath().toString());
//	}

}
