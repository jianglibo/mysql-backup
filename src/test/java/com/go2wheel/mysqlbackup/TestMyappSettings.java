package com.go2wheel.mysqlbackup;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.go2wheel.mysqlbackup.ui.MainMenuGroups;

@Import(com.go2wheel.mysqlbackup.TestMyappSettings.Tcc.class)
public class TestMyappSettings  extends SpringBaseFort {
	
	@Autowired
	private MainMenuGroups menuGroups;
	
	
	@Test
	public void t() {
		assertNotNull(myAppSettings.getSsh().getSshIdrsa());
		assertNotNull(myAppSettings.getSsh().getKnownHosts());
		
		assertThat(env.getProperty("logging.file"), equalTo("log/spring.log"));
		assertThat(env.getProperty("logging.file.max-size"), equalTo("5MB"));
		assertThat(env.getProperty("logging.file.max-history"), equalTo("100"));
		assertThat(env.getProperty("spring.profiles.active"), equalTo("dev"));
		
		assertThat(myAppSettings.getStorageExcludes().size(), greaterThan(4));
	}
	
	@Test
	public void testmenu() {
		assertThat(menuGroups.getGroups().size(), equalTo(3));
		assertThat(menuGroups.getGroups().get(0).getName(), equalTo("settings"));
		
		assertThat(menuGroups.getGroups().get(0).getItems().size(), equalTo(1));
		assertThat(menuGroups.getGroups().get(0).getItems().get(0).getPath(), equalTo("/app/settings"));
	}
	
	@Test
	public void tappstate() {
		String[] profiles = env.getActiveProfiles();
		assertThat(profiles.length, equalTo(1));
		assertTrue("should in dev profile.", Arrays.stream(env.getActiveProfiles()).anyMatch(p -> "dev".equals(p)));
	}
	
	@Value("#{tenv.aslist}")
	private List<String> pplist;
	
	@Value("#{tenv.oneStr}")
	private String helloStr;
	
	@Test
	public void tHello() {
		assertThat(helloStr, equalTo("hello"));
	}
	
	@Test
	public void tList() {
		assertThat(pplist.get(0), equalTo("abc"));
		assertThat(pplist.get(1), equalTo("ccd"));
		assertThat(pplist.size(), equalTo(2));
	}
	
	
	@TestConfiguration
	public static  class Tcc {
		
		@Bean
		public Tenv tenv() {
			return new Tenv();
		}
		
	}
	
	public static class Tenv {
		
		@Autowired
		private Environment env;
		
		private String oneStr = "hello";
		
		public List<String> getAslist() {
			String base = "ppp.aslist";
			List<String> values = new ArrayList<>();
			int i = 0;
			String v;
			while((v = env.getProperty("ppp.aslist[" + i + "]")) != null ) {
				values.add(v);
				i++;
			}
			return values;
		}

		public String getOneStr() {
			return oneStr;
		}

		public void setOneStr(String oneStr) {
			this.oneStr = oneStr;
		}
		
	}
}
