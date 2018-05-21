package com.go2wheel.mysqlbackup.job;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.DiskfreeService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.DiskFreeAllString;

@Component
public class DiskfreeJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private DiskfreeService diskfreeService;
	
	@Autowired
	private ServerService serverService;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		String host = data.getString("host");
		Box box = applicationState.getServerByHost(host);
		List<DiskFreeAllString> dfss = SSHcommonUtil.getDiskUsage(sshSessionFactory.getConnectedSession(box).getResult());
		List<Diskfree> dfs = dfss.stream().map(dd -> dd.toDiskfree()).collect(Collectors.toList());
		Server sv = serverService.findByHost(host);
		final Date d = new Date();
		if (sv != null) {
			dfs.stream().forEach(df -> {
				df.setCreatedAt(d);
				df.setServerId(sv.getId());
				diskfreeService.save(df);
			});
		}
	}

}
