package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class TestApplicationYml {

	@Test
	public void t() throws IOException {

		try (InputStream is = ClassLoader.class.getResourceAsStream("/application.yml")) {
			Map<String, Object> map = YamlInstance.INSTANCE.yaml.loadAs(is, Map.class);
			
			assertTrue(map.containsKey("only.you"));
			
			assertTrue(map.containsKey("myapp.dataDir"));
			
		}
	}

}
