package com.go2wheel.mysqlbackup.event;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.ReusableCron;

@Import(com.go2wheel.mysqlbackup.event.TestModelLifeCycleEvent.Tcc.class)
public class TestModelLifeCycleEvent  extends SpringBaseFort {
	
	private static int countTotal = 0;
	
	private static int countReu = 0;
	
	private static int countcc = 0;
	
	@Before
	public void b() {
		clearDb();
		TestModelLifeCycleEvent.countTotal = 0;
		TestModelLifeCycleEvent.countReu = 0;
		TestModelLifeCycleEvent.countcc = 0;
	}
	
	@Test
	public void tCreateModel() {
		ReusableCron rc = new ReusableCron("* * 7 * * ?", "ahel");
		rc = reuseableCronDbService.save(rc);
		assertThat(countReu, equalTo(1));
		assertThat(countTotal, equalTo(1));
		assertThat(countcc, equalTo(0));
		
		reuseableCronDbService.save(rc);
		assertThat(countReu, equalTo(1));
		assertThat(countTotal, equalTo(1));
		assertThat(countcc, equalTo(1));
	}
	

	
	@TestConfiguration
	public static  class Tcc {
		
		@Bean
		public Tenv tenv() {
			return new Tenv();
		}
		
	}
	
	public static class Tenv {
		@EventListener
		public void lis(ModelAfterCreatedEvent<ReusableCron> mce) {
			countReu++;
		}
		
		@EventListener
		public void lisu(ModelAfterCreatedEvent<?> mce) {
			countTotal++;
		}
		
		@EventListener
		public void c(ModelChangedEvent<?> mce) {
			countcc++;
		}

	}

}
