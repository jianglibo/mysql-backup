package com.go2wheel.mysqlbackup.value;

import java.text.ParseException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.service.KeyValueService;
import com.google.common.base.CaseFormat;

@ConfigurationProperties(prefix="dv")
@Component
public class DefaultValues {
	
	public static final String SERVER_STATE_CN = "server-state";
	public static final String STORAGE_STATE_CN = "storage-state";
	public static final String JOB_LOG_CN = "job-log";
	public static final String MYSQL_DUMP_CN = "mysql-dump";
	public static final String BORG_DOWNLOAD_CN = "borg-download";
	public static final String MYSQL_FLUSH_CN = "mysql-flush";

	public static final String DEFAULT_COUNT_PREFIX = "dv.default-count";
	
	@Autowired
	private KeyValueService keyValueService;

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Cron cron;
	
	private KeyValueProperties defaultCountOrigin;
	
	private KeyValueProperties defaultCount;
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	private boolean starting = true;
	
	private String getKey(String cn) {
		return String.format("%s.%s", DEFAULT_COUNT_PREFIX, cn);
	}
	
	public int getServerStateCount() {
		return settingsInDb.getInteger(getKey(SERVER_STATE_CN), 2);
	}
	
	public int getStorageStateCount() {
		return settingsInDb.getInteger(getKey(STORAGE_STATE_CN), 7);
	}

	public int getJobLogCount() {
		return settingsInDb.getInteger(getKey(JOB_LOG_CN), 20);
	}

	public int getMysqlDumpCount() {
		return settingsInDb.getInteger(getKey(MYSQL_DUMP_CN), 2);
	}

	public int getBorgDownloadCount() {
		return settingsInDb.getInteger(getKey(BORG_DOWNLOAD_CN), 20);
	}
	
	public int getFlushCount() {
		return settingsInDb.getInteger(getKey(MYSQL_FLUSH_CN), 10);
	}
	
	@PostConstruct
	public void post() throws ParseException {
		new CronExpression(cron.getBorgArchive());
		new CronExpression(cron.getBorgPrune());
		new CronExpression(cron.getStorageState());
		new CronExpression(cron.getMysqlFlush());
		new CronExpression(cron.getServerState());
		
		setDefaultCountOrigin(defaultCount);

		KeyValueProperties kvp = keyValueService.getPropertiesByPrefix(DEFAULT_COUNT_PREFIX);
		if (kvp.isEmpty()) {
			getDefaultCount().keySet().forEach(k -> {
				String kk = (String) k;
				kk = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN).convert(kk);
				KeyValue kv = new KeyValue(new String[] {DEFAULT_COUNT_PREFIX, kk}, getDefaultCount().getProperty(kk));
				keyValueService.save(kv);
			});
			starting = false;
		}
		kvp = keyValueService.getPropertiesByPrefix(DEFAULT_COUNT_PREFIX);
		kvp.setNext(getDefaultCount());
		setDefaultCount(kvp);
	}

	private void reLoadDefaultCount() {
		if (starting) return;
		KeyValueProperties kvp = keyValueService.getPropertiesByPrefix(DEFAULT_COUNT_PREFIX);
		kvp.setNext(getDefaultCount().getNext());
		setDefaultCount(kvp);
	}


	@EventListener
	public void whenKeyValueCreated(ModelCreatedEvent<KeyValue> keyValueCreatedEvent) {
		KeyValue kv = keyValueCreatedEvent.getModel();
		if (kv.getItemKey().startsWith(DEFAULT_COUNT_PREFIX)) {
			reLoadDefaultCount();
		}
	}
	
	@EventListener
	public void whenKeyValueChanged(ModelChangedEvent<KeyValue> keyValueChangedEvent) {
		KeyValue kv = keyValueChangedEvent.getAfter();
		if (kv.getItemKey().startsWith(DEFAULT_COUNT_PREFIX)) {
			reLoadDefaultCount();
		}
	}
	
	@EventListener
	public void whenKeyValueDeleted(ModelDeletedEvent<KeyValue> keyValueDeletedEvent) {
		KeyValue kv = keyValueDeletedEvent.getModel();
		if (kv.getItemKey().startsWith(DEFAULT_COUNT_PREFIX)) {
			reLoadDefaultCount();
		}
	}

	public Cron getCron() {
		return cron;
	}

	public void setCron(Cron cron) {
		this.cron = cron;
	}
	
	public KeyValueProperties getDefaultCount() {
		return defaultCount;
	}

	public void setDefaultCount(KeyValueProperties defaultCount) {
		this.defaultCount = defaultCount;
	}

	public KeyValueProperties getDefaultCountOrigin() {
		return defaultCountOrigin;
	}

	public void setDefaultCountOrigin(KeyValueProperties defaultCountOrigin) {
		this.defaultCountOrigin = defaultCountOrigin;
	}

	public static class DefaultCount {
		private int serverState;
		private int storageState;
		private int jobLog;
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
		public int getJobLog() {
			return jobLog;
		}
		public void setJobLog(int jobLog) {
			this.jobLog = jobLog;
		}
		
	}


	public static class Cron {
		private String serverState;
		private String storageState;
		private String borgArchive;
		private String borgPrune;
		private String mysqlFlush;
		
		private List<String> common;

		public String getServerState() {
			return serverState;
		}
		public void setServerState(String serverState) {
			this.serverState = serverState;
		}
		public String getStorageState() {
			return storageState;
		}
		public void setStorageState(String storageState) {
			this.storageState = storageState;
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
