package com.go2wheel.mysqlbackup.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;

public class TestSampleController extends SpringBaseTWithWeb {
	
	@Autowired
	private MockMvc mvc;
	
	
	@Test
	public void testModelAttribute() throws Exception {
		this.mvc.perform(get("/dynamic/a.ftl?number=5").accept(MediaType.TEXT_PLAIN))
		.andExpect(status().isOk()).andExpect(content().string("f5"));
		
		this.mvc.perform(get("/dynamic/a.html?number=5").accept(MediaType.TEXT_PLAIN))
		.andExpect(status().isOk()).andExpect(content().string("t5"));

	}
	
	@Test
	public void testModelAttributeSv() throws Exception {
		this.mvc.perform(get("/dynamic/ctx.ftl?number=5").accept(MediaType.TEXT_PLAIN))
		.andExpect(status().isOk()).andExpect(content().string("fdefault"));
		
		this.mvc.perform(get("/dynamic/ctx.html?number=5").accept(MediaType.TEXT_PLAIN))
		.andExpect(status().isOk()).andExpect(content().string("tdefault"));

	}

}
