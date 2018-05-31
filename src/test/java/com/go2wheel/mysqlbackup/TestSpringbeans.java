package com.go2wheel.mysqlbackup;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@Import(com.go2wheel.mysqlbackup.TestSpringbeans.Tcc.class)
public class TestSpringbeans  extends SpringBaseFort {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private Tenv tv3;
	
	@Autowired
	@Qualifier("amc")
	private Mc amc;
	
	@Test
	public void t() {
		Tenv tv1 = applicationContext.getBean("abean", Tenv.class);
		Tenv tv2 = applicationContext.getBean("bbean", Tenv.class);
		assertThat(tv1, equalTo(tv2));
		assertThat(tv2, equalTo(tv3));
	}
	
	@TestConfiguration
	public static  class Tcc {
		@Bean(name= {"abean", "bbean"})
		public Tenv tenv() {
			return new Tenv();
		}
		
		@Bean(name = "amc")
		public Mc mc() {
			return new Mc();
		}
		
		@Bean(name = "bmc")
		public Mc mc1() {
			return new Mc();
		}
	}
	
	public static class Mc {
		private int i =0;

		public int getI() {
			return i;
		}

		public void setI(int i) {
			this.i = i;
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
