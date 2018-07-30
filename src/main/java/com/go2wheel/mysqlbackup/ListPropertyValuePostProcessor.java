package com.go2wheel.mysqlbackup;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

public class ListPropertyValuePostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Properties props = new Properties();
		MutablePropertySources propSrcs = ((AbstractEnvironment) environment).getPropertySources();
		StreamSupport.stream(propSrcs.spliterator(), false)
		        .filter(ps -> ps instanceof EnumerablePropertySource)
		        .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
		        .flatMap(Arrays::<String>stream)
		        .forEach(propName -> props.setProperty(propName, environment.getProperty(propName)));
		
//		PropertySource<?> propertySource = null;
	}

}
