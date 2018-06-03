package com.go2wheel.mysqlbackup.util;


import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

import org.quartz.JobKey;
import org.quartz.TriggerKey;

import com.go2wheel.mysqlbackup.job.BorgBackupSchedule;
import com.go2wheel.mysqlbackup.job.DiskfreeSchedule;
import com.go2wheel.mysqlbackup.job.MysqlBackupSchedule;
import com.go2wheel.mysqlbackup.job.UpTimeSchedule;
import com.go2wheel.mysqlbackup.model.Server;

public class BoxUtil {
	
	public static JobKey getBorgArchiveJobKey(Server server) {
		return jobKey(server.getHost(), BorgBackupSchedule.BORG_ARCHIVE_GROUP);
	}
	
	public static TriggerKey getBorgPruneTriggerKey(Server server) {
		return triggerKey(server.getHost(), BorgBackupSchedule.BORG_PRUNE_GROUP);
	}

	
	public static JobKey getBorgPruneJobKey(Server server) {
		return jobKey(server.getHost(), BorgBackupSchedule.BORG_PRUNE_GROUP);
	}
	
	public static TriggerKey getBorgArchiveTriggerKey(Server server) {
		return triggerKey(server.getHost(), BorgBackupSchedule.BORG_ARCHIVE_GROUP);
	}
	
	public static JobKey getMysqlFlushLogJobKey(Server server) {
		return jobKey(server.getHost(), MysqlBackupSchedule.MYSQL_FLUSH_LOG_GROUP);
	}
	
	public static TriggerKey getMysqlFlushLogTriggerKey(Server server) {
		return triggerKey(server.getHost(), MysqlBackupSchedule.MYSQL_FLUSH_LOG_GROUP);
	}

//	public static JobKey getUpTimeJobKey(Server server) {
//		return jobKey(server.getHost(), UpTimeSchedule.UPTIME_GROUP);
//	}
//
//	public static TriggerKey getUpTimeTriggerKey(Server server) {
//		return triggerKey(server.getHost(), UpTimeSchedule.UPTIME_GROUP);
//	}

	public static JobKey getDiskfreeJobKey(Server server) {
		return jobKey(server.getHost(), DiskfreeSchedule.DISKFREE_GROUP);
	}

	public static TriggerKey getDiskfreeTriggerKey(Server server) {
		return triggerKey(server.getHost(), DiskfreeSchedule.DISKFREE_GROUP);
	}


}
