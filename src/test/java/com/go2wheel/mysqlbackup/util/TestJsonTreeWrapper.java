package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.SpringBaseFort;

public class TestJsonTreeWrapper extends SpringBaseFort {
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Value("${fort.borg.configuration.file}")
	private String borgconfigfile;
	
	@Test
	public void tj() throws IOException {
		JsonNode jn = objectMapper.readTree(Files.readAllBytes(Paths.get(borgconfigfile)));
		boolean b = new JsonTreeWrapper(jn).nodeValueEqual("SwitchByOs.centos.Softwares[0].InstallDetect.unexpect", "");
		assertTrue(b);
	}
	
	@Test
	public void tj1() throws IOException {
		JsonNode jn = objectMapper.readTree("{\"a\": null}");
		boolean b = new JsonTreeWrapper(jn).nodeValueEqual("a", null);
		assertTrue(b);
	}
	
	@Test
	public void tNodeExists() throws IOException {
		JsonNode jn = objectMapper.readTree("{\"a\": null}");
		boolean b = new JsonTreeWrapper(jn).nodeExists("a.b.c", "a");
		assertFalse(b);
	}

}
