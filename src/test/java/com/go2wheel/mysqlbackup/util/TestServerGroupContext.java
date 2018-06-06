package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;

public class TestServerGroupContext extends SpringBaseFort {
	
	@Test
	public void t() throws JsonParseException, JsonMappingException, IOException {
		Path pa = Paths.get("notingit", "tplcontext.json");
		ServerGroupContext sgc = objectMapper.readValue(pa.toFile(), ServerGroupContext.class);
		assertNotNull(sgc.getServerGroup());
	}

}
