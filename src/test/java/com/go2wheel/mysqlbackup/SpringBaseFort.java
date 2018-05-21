package com.go2wheel.mysqlbackup;

import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.BackupFolderService;
import com.go2wheel.mysqlbackup.service.MysqlDumpService;
import com.go2wheel.mysqlbackup.service.MysqlFlushService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.BorgBackupDescription;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.jcraft.jsch.Session;

@SpringBootTest({ "spring.shell.interactive.enabled=false" })
@RunWith(SpringRunner.class)
public class SpringBaseFort {
	public static final String HOST_DEFAULT = "192.168.33.110";

	@Autowired
	protected MyAppSettings myAppSettings;

	@Autowired
	protected Environment env;

	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private MysqlFlushService mysqlFlushService;
	
	@Autowired
	private MysqlDumpService mysqlDumpService;
	
	@Autowired
	private BackupFolderService backupFolderService;
	
	protected Box box;
	
	@Autowired
	private DefaultValues dvs;
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private ApplicationState applicationState;
	
	protected Session session;
	
	@Before
	public void before() throws SchedulerException {
		deleteAllJobs();
		box = createBox();
		session = sshSessionFactory.getConnectedSession(box).getResult();
		applicationState.setServers(new ArrayList<>());
		applicationState.getServers().add(box);
		
		mysqlDumpService.deteteAll();
		mysqlFlushService.deteteAll();
		backupFolderService.deteteAll();
		serverService.deteteAll();
		Server sv = new Server(box.getHost());
		serverService.save(sv);
	}
	

	protected void deleteAllJobs() throws SchedulerException {
		for (JobKey jk : allJobs()) {
			scheduler.deleteJob(jk);
		}
		;
	}

	protected List<JobKey> allJobs() throws SchedulerException {
		List<JobKey> jks = new ArrayList<>();
		for (String groupName : scheduler.getJobGroupNames()) {
			jks.addAll(scheduler.getJobKeys(groupEquals(groupName)));
		}
		return jks;
	}
	
	private Box createBox() {
		box = new Box();
		box.setHost(HOST_DEFAULT);
		box.setBorgBackup(new BorgBackupDescription());
		box.setMysqlInstance(new MysqlInstance());
		
		box.getMysqlInstance().setPassword("123456");
		
		box.getBorgBackup().setArchiveCron(dvs.getCron().getBorgArchive());
		box.getBorgBackup().setPruneCron(dvs.getCron().getBorgPrune());
		box.getMysqlInstance().setFlushLogCron(dvs.getCron().getMysqlFlush());
		
		
		Map<String, String> map = new HashMap<>();
		map.put("log_bin", "ON");
		map.put("log_bin_basename", "/var/lib/mysql/hm-log-bin");
		map.put("log_bin_index", "/var/lib/mysql/hm-log-bin.index");
		LogBinSetting lbs = new LogBinSetting(map);
		box.getMysqlInstance().setLogBinSetting(lbs);
		return box;
	}
	
	protected void boxDeleteLogBinSetting() {
		box.getMysqlInstance().setLogBinSetting(null);
	}

}
