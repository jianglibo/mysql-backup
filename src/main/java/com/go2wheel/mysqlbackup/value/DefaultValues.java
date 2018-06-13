package com.go2wheel.mysqlbackup.value;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="dv")
@Component
public class DefaultValues {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Cron cron;
	
	private DefaultCount defaultCount;
	
	@PostConstruct
	public void post() throws ParseException {
		new CronExpression(cron.getBorgArchive());
		new CronExpression(cron.getBorgPrune());
		new CronExpression(cron.getDiskfree());
		new CronExpression(cron.getMysqlFlush());
		new CronExpression(cron.getUptime());
	}

	public Cron getCron() {
		return cron;
	}

	public void setCron(Cron cron) {
		this.cron = cron;
	}
	
	public DefaultCount getDefaultCount() {
		return defaultCount;
	}

	public void setDefaultCount(DefaultCount defaultCount) {
		this.defaultCount = defaultCount;
	}

	public static class DefaultCount {
		private int serverState;
		private int storageState;
		private int jobError;
		private int mysqlDump;
		private int borgDownload;
		
		private int mysqlFlush;
		
		public int getServerState() {
			return serverState;
		}
		public void setServerState(int serverState) {
			this.serverState = serverState;
		}
		public int getStorageState() {
			return storageState;
		}
		public void setStorageState(int storageState) {
			this.storageState = storageState;
		}
		public int getJobError() {
			return jobError;
		}
		public void setJobError(int jobError) {
			this.jobError = jobError;
		}
		public int getMysqlDump() {
			return mysqlDump;
		}
		public void setMysqlDump(int mysqlDump) {
			this.mysqlDump = mysqlDump;
		}
		public int getBorgDownload() {
			return borgDownload;
		}
		public void setBorgDownload(int borgDownload) {
			this.borgDownload = borgDownload;
		}
		public int getMysqlFlush() {
			return mysqlFlush;
		}
		public void setMysqlFlush(int mysqlFlush) {
			this.mysqlFlush = mysqlFlush;
		}
		
	}


	public static class Cron {
		private String uptime;
		private String diskfree;
		private String borgArchive;
		private String borgPrune;
		private String mysqlFlush;
		
		private List<String> common;
		
		public String getUptime() {
			return uptime;
		}
		public void setUptime(String uptime) {
			this.uptime = uptime;
		}
		public String getDiskfree() {
			return diskfree;
		}
		public void setDiskfree(String diskfree) {
			this.diskfree = diskfree;
		}
		public String getBorgArchive() {
			return borgArchive;
		}
		public void setBorgArchive(String borgArchive) {
			this.borgArchive = borgArchive;
		}
		public String getBorgPrune() {
			return borgPrune;
		}
		public void setBorgPrune(String borgPrune) {
			this.borgPrune = borgPrune;
		}
		public String getMysqlFlush() {
			return mysqlFlush;
		}
		public void setMysqlFlush(String mysqlFlush) {
			this.mysqlFlush = mysqlFlush;
		}
		public List<String> getCommon() {
			return common;
		}
		public void setCommon(List<String> common) {
			this.common = common;
		}


	}
}
