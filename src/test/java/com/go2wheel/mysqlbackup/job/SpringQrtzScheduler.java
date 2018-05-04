package com.go2wheel.mysqlbackup.job;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
//@ConditionalOnExpression("'${using.spring.schedulerFactory}'=='true'")
public class SpringQrtzScheduler {

    Logger logger = LoggerFactory.getLogger(getClass());
    
    public static final String GROUP_NAME = "FOR_TEST_GROUP";

    @Autowired
    private ApplicationContext applicationContext;
    
    @PostConstruct
    public void init() {
        logger.info("Hello world from Spring...");
    }

//    @Bean
//    public SpringBeanJobFactory springBeanJobFactory() {
//        AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
//        logger.debug("Configuring Job factory");
//
//        jobFactory.setApplicationContext(applicationContext);
//        return jobFactory;
//    }
    
//    org.springframework.boot.autoconfigure.quartz
//	@Bean
//	@ConditionalOnMissingBean
//	public SchedulerFactoryBean quartzScheduler() {
		

//    @Bean
//    public SchedulerFactoryBean scheduler(Trigger trigger, JobDetail job) {
//
//        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
//        schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));
//        Properties p = new Properties();
//
//        logger.debug("Setting the Scheduler up");
//        schedulerFactory.setJobFactory(springBeanJobFactory());
//        schedulerFactory.setJobDetails(job);
//        schedulerFactory.setTriggers(trigger);
//
//        return schedulerFactory;
//    }

    @Bean
    public JobDetailFactoryBean sampleJobDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(SampleJob.class);
        jobDetailFactory.setName("Qrtz_Job_Detail");
        jobDetailFactory.setGroup(GROUP_NAME);
        jobDetailFactory.setDescription("Invoke Sample Job service...");
        jobDetailFactory.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("date", new Date());
        jobDetailFactory.setJobDataMap(jobDataMap);
        return jobDetailFactory;
    }
    

    @Bean
    public SimpleTriggerFactoryBean sampleJobTrigger() {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(sampleJobDetail().getObject());

        int frequencyInSec = 10;
        logger.info("Configuring trigger to fire every {} seconds", frequencyInSec);

        trigger.setRepeatInterval(frequencyInSec * 1000);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName("Qrtz_Trigger");
        trigger.setGroup(GROUP_NAME);
        return trigger;
    }
    
    
//    @Bean
//    public JobDetailFactoryBean sampleJobDetail1() {
//        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
//        jobDetailFactory.setJobClass(SampleJob.class);
//        jobDetailFactory.setName("Qrtz_Job_Detail");
//        jobDetailFactory.setDescription("Invoke Sample Job service...");
//        jobDetailFactory.setDurability(true);
//        return jobDetailFactory;
//    }

//    
//    @Bean
//    public CronTriggerFactoryBean sampleJobTrigger2() {
//    	CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
//        trigger.setJobDetail(sampleJobDetail().getObject());
//        trigger.setCronExpression("0 0 12 * * ?");
//        trigger.setName("Qrtz_Trigger_2");
//        trigger.setGroup(GROUP_NAME);
//        return trigger;
//    }
//    
//    @Bean
//    public CronTriggerFactoryBean sampleJobTrigger3() {
//    	CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
//        trigger.setJobDetail(sampleJobDetail().getObject());
//        trigger.setCronExpression("0 0 12 * * ?");
//        trigger.setName("Qrtz_Trigger_2");
//        return trigger;
//    }

//    @Bean
//    public SimpleTriggerFactoryBean sampleJobTrigger1() {
//        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
//        trigger.setJobDetail(sampleJobDetail().getObject());
//
//        int frequencyInSec = 10;
//        logger.info("Configuring trigger to fire every {} seconds", frequencyInSec);
//
//        trigger.setRepeatInterval(frequencyInSec * 1000);
//        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
//        trigger.setName("Qrtz_Trigger_1");
//        trigger.setGroup(GROUP_NAME);
//        return trigger;
//    }

}
