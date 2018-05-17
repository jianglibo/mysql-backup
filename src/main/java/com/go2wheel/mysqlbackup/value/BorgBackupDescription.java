package com.go2wheel.mysqlbackup.value;

import java.util.ArrayList;
import java.util.List;

import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class BorgBackupDescription {

	public static final String BORG_ARCHIVE_PREFIX_DEFAULT = "ARCHIVE-";
	public static final String BORG_REPO_DEFAULT = "/opt/borgrepos/repo";
	public static final String BORG_ARCHIVE_FORMAT_DEFAULT = "yyyy-MM-dd-HH-mm-ss";
	
	private String repo = BORG_REPO_DEFAULT;
	private List<String> includes = new ArrayList<>();
	private List<String> excludes = new ArrayList<>();
	private String archiveFormat = BORG_ARCHIVE_FORMAT_DEFAULT;
	private String archiveCron;
	private String pruneCron;
	private String archiveNamePrefix = BORG_ARCHIVE_PREFIX_DEFAULT;
	
	
	@Override
	public String toString() {
		return YamlInstance.INSTANCE.yaml.dumpAsMap(this);
	}
	
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

	public String getArchiveNamePrefix() {
		return archiveNamePrefix;
	}

	public void setArchiveNamePrefix(String archiveNamePrefix) {
		this.archiveNamePrefix = archiveNamePrefix;
	}

	public String getPruneCron() {
		return pruneCron;
	}

	public void setPruneCron(String pruneCron) {
		this.pruneCron = pruneCron;
	}

	public String getArchiveCron() {
		return archiveCron;
	}

	public void setArchiveCron(String archiveCron) {
		this.archiveCron = archiveCron;
	}
}
