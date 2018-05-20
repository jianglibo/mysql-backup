package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.UpTime;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.service.UpTimeService;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.UptimeAllString;

@Component
public class UpTimeJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private UpTimeService upTimeService;
	
	@Autowired
	private ServerService serviceService;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		String host = data.getString("host");
		Box box = applicationState.getServerByHost(host);
		UptimeAllString uta = SSHcommonUtil.getUpTime(sshSessionFactory.getConnectedSession(box).getResult());
		UpTime ut = uta.toUpTime();
		Server sv = serviceService.findByHost(host);
		if (sv != null) {
			ut.setServerId(sv.getId());
			upTimeService.save(ut);
		}
	}

}
