package com.go2wheel.mysqlbackup.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;

public class TestComboController extends SpringBaseTWithWeb {

	@Autowired
	private MockMvc mvc;

	@Test
	public void testCombo() throws Exception {

		MvcResult result = this.mvc.perform(get("/combo/1.82.3?/t/hello.js")).andReturn();

		this.mvc.perform(asyncDispatch(result)).andExpect(status().isOk())
				.andExpect(header().string("Content-Type", ComboController.APPLICATION_JS))
				.andExpect(content().string("hello js."));
	}

	@Test
	public void testComboEmpty() throws Exception {
		MvcResult result = this.mvc.perform(get("/combo/1.82.3")).andReturn();
		this.mvc.perform(asyncDispatch(result)).andExpect(status().is(HttpStatus.NOT_FOUND.value()))
				.andExpect(content().string("empty file list."));
	}

	@Test
	public void testComboMixed() throws Exception {
		MvcResult result = this.mvc.perform(get("/combo/1.82.3?/t/hello.js&/t/hello.css")).andReturn();
		this.mvc.perform(asyncDispatch(result)).andExpect(status().is(HttpStatus.NOT_FOUND.value()))
				.andExpect(content().string("mixed file type."));
	}
	
	@Test
	public void testComboMissing() throws Exception {
		MvcResult result = this.mvc.perform(get("/combo/1.82.3?/t/hello.js&/t/hello1.js")).andReturn();
		this.mvc.perform(asyncDispatch(result)).andExpect(status().is(HttpStatus.NOT_FOUND.value()))
				.andExpect(content().string("some files not exists."));
	}


	@Test
	public void testImg() throws Exception {
		this.mvc.perform(get("/img/layout-icon.jpg")).andExpect(status().isOk());
	}

	@Test
	public void tResourcePtn() throws IOException {
		Resource[] jsrs = wac.getResources("classpath:public/**/*.js");
		Resource[] csrs = wac.getResources("classpath:public/**/*.css");

		assertThat(jsrs.length, equalTo(18));
		assertThat(csrs.length, equalTo(17));
	}

}
