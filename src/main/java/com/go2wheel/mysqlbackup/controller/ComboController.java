package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.google.common.base.Splitter;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

@Controller
@RequestMapping(method = RequestMethod.GET, value="/combo")
public class ComboController implements ApplicationContextAware {
	
	protected static final String TEXT_CSS = "text/css; charset=utf-8"; 
	protected static final String APPLICATION_JS = "application/javascript; charset=utf-8";
	
	private ApplicationContext applicationContext;
	
	@Autowired
	private MyAppSettings myAppsettings;
	
	private StreamingResponseBody errorRb(String errorMsg) {
		return new StreamingResponseBody() {
	        @Override
	        public void writeTo(OutputStream outputStream) throws IOException {
	        	outputStream.write(errorMsg.getBytes());
	        	outputStream.flush();
	        }
	    };
	}
	
	@GetMapping("/{version}")
	public ResponseEntity<StreamingResponseBody> getVersion(@PathVariable String version, HttpServletRequest request) {
		String qs = request.getQueryString();
		if (qs == null) qs = "";
		List<String> fns = Splitter.on('&').omitEmptyStrings().splitToList(qs);
		if (fns.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRb("empty file list."));
		}
		String ct; 
		if (isAllSameFiles("css", fns)) {
			ct = TEXT_CSS;
		} else if (isAllSameFiles("js", fns)){
			ct = APPLICATION_JS;
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRb("mixed file type."));
		}
		List<Resource> ress = fns.stream().map(this::fnToRe).filter(rs -> rs.exists()).collect(Collectors.toList());
		
		if (ress.size() != fns.size()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRb("some files not exists."));
		}
		StreamingResponseBody srb = new StreamingResponseBody() {
	        @Override
	        public void writeTo(OutputStream outputStream) throws IOException {
	        	for(Resource rs: ress) {
	        		try(InputStream is = rs.getInputStream()) {
	        			ByteStreams.copy(is, outputStream);
	        		}
	        	}
	        	outputStream.flush();
	        }
	    };
	    return ResponseEntity
	            .ok()
	            .header("Content-Type", ct)
	            .cacheControl(CacheControl.maxAge(myAppsettings.getCache().getCombo(), TimeUnit.DAYS))
	            .eTag(version) // lastModified is also available
	            .body(srb);
	}
	
	private boolean isAllSameFiles(String specificExt, List<String> fns) {
		return !fns.stream().map(Files::getFileExtension).anyMatch(ext -> !specificExt.equalsIgnoreCase(ext));
	}
	
	private Resource fnToRe(String fn) {
		String uri = "classpath:public" + fn;
		return applicationContext.getResource(uri);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
