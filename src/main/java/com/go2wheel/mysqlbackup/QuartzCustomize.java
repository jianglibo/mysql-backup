package com.go2wheel.mysqlbackup;

import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class QuartzCustomize implements SchedulerFactoryBeanCustomizer {

	@Override
	public void customize(SchedulerFactoryBean schedulerFactoryBean) {
		
		System.out.println(schedulerFactoryBean);
		
	}

}
