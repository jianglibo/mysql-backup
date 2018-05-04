package com.go2wheel.mysqlbackup.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SampleJob implements Job {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SampleJobService jobService;
    
    private Date date;

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        jobService.executeSampleJob();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Date date = (Date) jobDataMap.get("date");

        logger.info("Next job scheduled @ {}", context.getNextFireTime());
    }

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
