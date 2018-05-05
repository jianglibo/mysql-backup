package com.go2wheel.mysqlbackup.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
public class BackupQrtzScheduler {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext applicationContext;
    
    @Bean
    public JobDetailFactoryBean mysqlFlushLogJobDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(MysqlFlushLogJob.class);
        jobDetailFactory.setName("mysql_flush_logs");
        jobDetailFactory.setGroup("MYSQL");
        jobDetailFactory.setDescription("Flush mysql logs and download new created log files.");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }
   
//    http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html
    
    // 0 30 6,12 * * ? Fire at 6:30am and 12:30pm every day. 
    
    @Bean
    public CronTriggerFactoryBean mysqlFlushLogsJobTrigger() {
    	CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(mysqlFlushLogJobDetail().getObject());
        trigger.setCronExpression("0 0 12 * * ?");
        trigger.setGroup("MYSQL");
        trigger.setName("mysql_flush_logs");
        return trigger;
    }
}
