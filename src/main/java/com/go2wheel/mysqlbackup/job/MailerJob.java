package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.go2wheel.mysqlbackup.service.ServerGrpService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.service.UpTimeService;
import com.go2wheel.mysqlbackup.service.UserAccountService;
import com.go2wheel.mysqlbackup.service.UserServerGrpService;

@Component
public class MailerJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UpTimeService upTimeService;
	
	@Autowired
	private UserServerGrpService userServerGrpService;
	
	@Autowired
	private UserAccountService userAccountService;
	
	@Autowired
	private ServerGrpService serverGrpService;
	
	@Autowired
	private ServerService serverService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int userServerGrpId = data.getInt("id");
		UserServerGrp usg = userServerGrpService.findById(userServerGrpId);
		
		if (usg == null) {
			logger.error("Cannot find UserServerGrp with ID: {}", userServerGrpId);
			return;
		}
		
		ServerGrp sg = serverGrpService.findById(usg.getServerGrpId());
		UserAccount ua = userAccountService.findById(usg.getUserAccountId());
	}

}
