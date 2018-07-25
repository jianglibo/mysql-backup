package com.go2wheel.mysqlbackup.ui;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;

import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;


public class TestFormSubmit extends SpringBaseTWithWeb {

	/**
	 * test form submit.
	 * 
	 * @throws Exception
	 * 
	 */

	@Test
	public void testMutilpleName() throws Exception {
		mockMvc.perform(post("/test/abc").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("a", "1").param("a", "2"))
				.andExpect(status().isOk())
				.andExpect(content().string("3"));
	}

}
