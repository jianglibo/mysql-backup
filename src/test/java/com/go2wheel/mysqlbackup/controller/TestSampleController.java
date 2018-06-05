package com.go2wheel.mysqlbackup.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestSampleController extends SpringBaseTWithWeb {
	
	@Autowired
	private MockMvc mvc;
	
//	@Test
//	public void tRegex() throws Exception {
//		this.mvc.perform(get("/spring-web-3.0.5.jar").accept(MediaType.TEXT_PLAIN))
//		.andExpect(status().isOk());
//
//	}
	
//	@Test
//	public void testUrlbased() throws Exception {
//		this.mvc.perform(get("/t.ftl").accept(MediaType.TEXT_PLAIN))
//		.andExpect(status().isOk());
//	}
	
	
	@Test
	public void testModelAttribute() throws Exception {
		this.mvc.perform(get("/freemarker/ctx.ftl?number=5").accept(MediaType.TEXT_PLAIN))
		.andExpect(status().isOk()).andExpect(content().string("5"));
	}
	
//	@Test
//	public void testThymeleafExample() throws Exception {
//		this.mvc.perform(get("/thymeleaf/a.html").accept(MediaType.TEXT_PLAIN))
//				.andExpect(status().isOk()).andExpect(content().string("thymeleaf."));
//	}

	
	

}
