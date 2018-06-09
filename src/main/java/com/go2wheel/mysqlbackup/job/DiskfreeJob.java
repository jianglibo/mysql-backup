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

import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.DiskfreeDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.DiskFreeAllString;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.Session;

@Component
public class DiskfreeJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DiskfreeDbService diskfreeDbService;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Session session = null;
		try {
			JobDataMap data = context.getMergedJobDataMap();
			int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
			Server box = serverDbService.findById(sid);
			
			FacadeResult<Session> fr = sshSessionFactory.getConnectedSession(box); 
			session = fr.getResult();
			if (session == null) {
				logger.error("Connecting to server {} failed. message is: {}", box.getHost(), fr.getMessage());
				return;
			}
			List<DiskFreeAllString> dfss = SSHcommonUtil.getDiskUsage(session);
			List<Diskfree> dfs = dfss.stream().map(dd -> dd.toDiskfree()).collect(Collectors.toList());
			Server sv = serverDbService.findByHost(box.getHost());
			final Date d = new Date();
			if (sv != null) {
				dfs.stream().forEach(df -> {
					df.setCreatedAt(d);
					df.setServerId(sv.getId());
					diskfreeDbService.save(df);
				});
			}
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
