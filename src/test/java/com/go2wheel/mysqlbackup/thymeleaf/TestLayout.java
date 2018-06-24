package com.go2wheel.mysqlbackup.thymeleaf;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;

public class TestLayout  extends SpringBaseTWithWeb {
	
	@Autowired
	private MockMvc mvc;
	
	
	@Test
	public void testModelAttribute() throws Exception {
		this.mvc.perform(get("/dynamic/uselayout/page1.html").accept(MediaType.TEXT_PLAIN))
		.andExpect(status().isOk()).andExpect(content().string(containsString("Page1 content")));
		
		this.mvc.perform(get("/dynamic/uselayout/page1.html").accept(MediaType.TEXT_PLAIN))
		.andExpect(status().isOk()).andExpect(content().string(containsString("Page1 Title")));
		
		this.mvc.perform(get("/dynamic/uselayout/page2.html").accept(MediaType.TEXT_PLAIN))
		.andExpect(status().isOk()).andExpect(content().string(containsString("Page2 content")));
		
		this.mvc.perform(get("/dynamic/uselayout/page2.html").accept(MediaType.TEXT_PLAIN))
		.andExpect(status().isOk()).andExpect(content().string(containsString("Page2 Title")));

	}
}
