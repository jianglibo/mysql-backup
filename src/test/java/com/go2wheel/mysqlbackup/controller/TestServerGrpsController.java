package com.go2wheel.mysqlbackup.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;

public class TestServerGrpsController extends SpringBaseTWithWeb {
	
	@Test
	public void testDeleteServerRelation() throws Exception {
		
		this.mockMvc.perform(delete("/app/server-grps/106/servers").content("{'server': 100}").contentType("application/x-www-form-urlencoded; charset=UTF-8"))
		.andExpect(status().isOk()).andExpect(content().string("Hello World!"));

	}

}
