package com.go2wheel.mysqlbackup.jooq;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.jooq.util.GenerationTool;
import org.jooq.util.jaxb.Configuration;
import org.jooq.util.jaxb.Jdbc;
import org.junit.Before;
import org.junit.Test;

public class TestJooqCodegen {

	private String jdbcUrl;
	
	public static final String JOOQ_CONFIG_FILE = "/jooq-config.xml";
	
	@Before
	public void be() throws IOException {
		Properties p = new Properties();
		try (InputStream is = Files.newInputStream(Paths.get("gradle.properties"))) {
			p.load(is);
		}
		jdbcUrl = p.getProperty("flyway.url");
	}
	
	@Test
	public void codegen() throws Exception {
		InputStream in = TestJooqCodegen.class.getResourceAsStream(JOOQ_CONFIG_FILE);
		Configuration cfg = GenerationTool.load(in);
		cfg.setJdbc(new Jdbc().withUrl(jdbcUrl).withUser("SA").withPassword(""));
		GenerationTool.generate(cfg);
		in.close();
	}
}
