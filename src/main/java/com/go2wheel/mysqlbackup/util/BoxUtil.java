package com.go2wheel.mysqlbackup.util;


import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import com.go2wheel.mysqlbackup.job.BorgBackupSchedule;
import com.go2wheel.mysqlbackup.job.MysqlBackupSchedule;
import com.go2wheel.mysqlbackup.job.UpTimeSchedule;
import com.go2wheel.mysqlbackup.value.Box;

public class BoxUtil {
	
	public static JobKey getBorgArchiveJobKey(Box box) {
		return jobKey(box.getHost(), BorgBackupSchedule.BORG_ARCHIVE_GROUP);
	}
	
	public static TriggerKey getBorgPruneTriggerKey(Box box) {
		return triggerKey(box.getHost(), BorgBackupSchedule.BORG_PRUNE_GROUP);
	}

	
	public static JobKey getBorgPruneJobKey(Box box) {
		return jobKey(box.getHost(), BorgBackupSchedule.BORG_PRUNE_GROUP);
	}
	
	public static TriggerKey getBorgArchiveTriggerKey(Box box) {
		return triggerKey(box.getHost(), BorgBackupSchedule.BORG_ARCHIVE_GROUP);
	}
	
	public static JobKey getMysqlFlushLogJobKey(Box box) {
		return jobKey(box.getHost(), MysqlBackupSchedule.MYSQL_FLUSH_LOG_GROUP);
	}
	
	public static TriggerKey getMysqlFlushLogTriggerKey(Box box) {
		return triggerKey(box.getHost(), MysqlBackupSchedule.MYSQL_FLUSH_LOG_GROUP);
	}

	public static JobKey getUpTimeJobKey(Box box) {
		return jobKey(box.getHost(), UpTimeSchedule.UPTIME_GROUP);
	}

	public static TriggerKey getUpTimeTriggerKey(Box box) {
		return triggerKey(box.getHost(), UpTimeSchedule.UPTIME_GROUP);
	}


}
