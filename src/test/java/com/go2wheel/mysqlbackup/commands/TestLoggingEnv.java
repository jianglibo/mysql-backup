package com.go2wheel.mysqlbackup.commands;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.google.common.collect.Maps;

public class TestLoggingEnv extends SpringBaseFort {
	
	@SuppressWarnings("rawtypes")
	@Test
	public void t() {
//		String[] ss = env.getDefaultProfiles();
        Map<String, Object> map = Maps.newHashMap();
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
