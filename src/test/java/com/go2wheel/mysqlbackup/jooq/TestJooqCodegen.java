package com.go2wheel.mysqlbackup.jooq;

import java.io.InputStream;

import org.jooq.util.GenerationTool;
import org.jooq.util.jaxb.Configuration;
import org.jooq.util.jaxb.Jdbc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestJooqCodegen {
	
	@Value("${spring.datasource.url}")
	private String jdbcUrl;
	
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
