package com.go2wheel.mysqlbackup.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;

public class TestHomeController extends SpringBaseTWithWeb {

	@Autowired
	private MockMvc mvc;

	@Test
	public void testModelAttribute() throws Exception {
		this.mvc.perform(get("/").accept(MediaType.TEXT_PLAIN)).andExpect(status().isOk())
				.andExpect(content()
						.string(containsString("class=\"pure-menu-item menu-item-divided pure-menu-selected\"")));
//				.andExpect(xpath("//ul[@class='pure-menu-list']").exists());
	}

}
