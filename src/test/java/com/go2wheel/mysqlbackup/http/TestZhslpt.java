package com.go2wheel.mysqlbackup.http;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.util.PathUtil;

public class TestZhslpt extends SpringBaseFort {
	
	@Autowired
	private ObjectMapper om;
	
	@Test
	public void tgly() throws IOException {
		String name = PathUtil.getClassPathResourceName(this.getClass(), "gly.json");
		Resource rs = applicationContext.getResource(name);
		try (InputStream is = rs.getInputStream()) {
		  JsonNode jn =	om.readTree(is);
		  jn = jn.get("content");
		}
		
	}

}
