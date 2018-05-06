package com.go2wheel.mysqlbackup.value;

import java.util.List;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class BorgBackupDescription {

	public static final String BORG_ARCHIVE_PREFIX_DEFAULT = "ARCHIVE-";
	
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
		if (!StringUtil.hasAnyNonBlankWord(archiveNamePrefix) || "null".equals(archiveNamePrefix)) {
			return BORG_ARCHIVE_PREFIX_DEFAULT;
		}
		return archiveNamePrefix;
	}

	public void setArchiveNamePrefix(String archiveNamePrefix) {
		this.archiveNamePrefix = archiveNamePrefix;
	}
}
