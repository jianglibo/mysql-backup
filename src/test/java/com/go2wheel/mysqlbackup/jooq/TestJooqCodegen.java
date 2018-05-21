package com.go2wheel.mysqlbackup.jooq;

import java.io.InputStream;

import org.jooq.util.GenerationTool;
import org.jooq.util.jaxb.Configuration;
import org.jooq.util.jaxb.Jdbc;
import org.junit.Test;

public class TestJooqCodegen {

	private String jdbcUrl = "jdbc:hsqldb:file:c:/db/mysqlbackup/devdb;shutdown=true";
	
	public static final String JOOQ_CONFIG_FILE = "/jooq-config.xml";
	
	@Test
	public void codegen() throws Exception {
		InputStream in = TestJooqCodegen.class.getResourceAsStream(JOOQ_CONFIG_FILE);
		Configuration cfg = GenerationTool.load(in);
		cfg.setJdbc(new Jdbc().withUrl(jdbcUrl).withUser("SA").withPassword(""));
		GenerationTool.generate(cfg);
		in.close();
	}
}
