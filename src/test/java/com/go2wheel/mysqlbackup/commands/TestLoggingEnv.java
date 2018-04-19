package com.go2wheel.mysqlbackup.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestLoggingEnv {
	
	@Autowired
	private Environment env;
	
//	@Value("${org.springframework.boot.logging.LoggingSystem}")
//	private String logsys;
	
//	logging.config
//	org.springframework.boot.logging.LoggingSystem
	
	@Test
	public void t() {
		String[] ss = env.getDefaultProfiles();
        Map<String, Object> map = new HashMap();
        for(Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext(); ) {
            PropertySource propertySource = (PropertySource) it.next();
            if (propertySource instanceof MapPropertySource) {
                map.putAll(((MapPropertySource) propertySource).getSource());
            }
        }
        
        for(Entry<String, Object> entry: map.entrySet()) {
        	String k = entry.getKey();
        	Object v = entry.getValue();
        	if (k.indexOf("log") != -1) {
        		System.out.println(k + ": " + v);
        	}
        }
        
		String s = env.getProperty("logging.config");
		System.out.println(s);
	}

}
