package com.go2wheel.mysqlbackup.value;

import java.util.List;

public class BorgBackupDescription {
	
	private String repo;
	private List<String> includes;
	private List<String> excludes;
	private String archiveFormat;
	
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
}
