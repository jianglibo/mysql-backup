package com.go2wheel.mysqlbackup.ui;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.Test;
import org.springframework.http.MediaType;

import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;

public class TestFormSubmit extends SpringBaseTWithWeb {

	/**
	 * test form submit.
	 * @throws Exception 
	 * 
	 */

	@Test
	public void testMutilpleName() throws Exception {
		mockMvc.perform(post("abc").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("a", "1").param("a", "2"))
				.andExpect(content().string(containsString("hello")));
	}

}
