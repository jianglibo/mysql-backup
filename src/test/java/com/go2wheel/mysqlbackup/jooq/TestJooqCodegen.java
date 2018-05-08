package com.go2wheel.mysqlbackup.jooq;

import java.io.InputStream;

import org.jooq.util.GenerationTool;
import org.jooq.util.jaxb.Configuration;
import org.junit.Test;


public class TestJooqCodegen {
	
	public static final String JOOQ_CONFIG_FILE = "/jooq-config.xml";
	
	@Test
	public void codegen() throws Exception {
		InputStream in = TestJooqCodegen.class.getResourceAsStream(JOOQ_CONFIG_FILE);
		Configuration cfg = GenerationTool.load(in);
		GenerationTool.generate(cfg);
		in.close();
	}
	


}
