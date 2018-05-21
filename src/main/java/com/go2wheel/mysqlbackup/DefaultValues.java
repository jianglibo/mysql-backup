package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.text.ParseException;

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

	public static class Cron {
		private String uptime;
		private String diskfree;
		private String borgArchive;
		private String borgPrune;
		private String mysqlFlush;
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


	}
}
