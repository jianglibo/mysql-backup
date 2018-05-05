package com.go2wheel.mysqlbackup.value;

import java.util.List;

public class BorgBackupDescription {
	
	private String repo;
	private List<String> includes;
	private List<String> excludes;
	private String archiveFormat;
	private String cronExpression;
	private String archiveNamePrefix;
	
	public String getRepo() {
		return repo;
	}
	
	public void setRepo(String repo) {
		this.repo = repo;
	}
	public List<String> getIncludes() {
		return includes;
	}
	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}

	public String getArchiveFormat() {
		return archiveFormat;
	}
	public void setArchiveFormat(String archiveFormat) {
		this.archiveFormat = archiveFormat;
	}

	public List<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getArchiveNamePrefix() {
		return archiveNamePrefix;
	}

	public void setArchiveNamePrefix(String archiveNamePrefix) {
		this.archiveNamePrefix = archiveNamePrefix;
	}
}
