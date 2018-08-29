package com.go2wheel.mysqlbackup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class BorgDescription extends BaseModel {

	public static final String BORG_ARCHIVE_PREFIX_DEFAULT = "ARCHIVE-";
	public static final String BORG_REPO_DEFAULT = "/opt/borgrepos/repo";
	public static final String BORG_ARCHIVE_FORMAT_DEFAULT = "yyyy-MM-dd-HH-mm-ss";
	
	private String repo = BORG_REPO_DEFAULT;
	private List<String> includes = new ArrayList<>();
	private List<String> excludes = new ArrayList<>();

	private String archiveFormat = BORG_ARCHIVE_FORMAT_DEFAULT;
	@CronExpressionConstraint(allowEmpty=true)
	private String archiveCron;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String pruneCron;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String localBackupCron;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String localBackupPruneCron;

	
	private String archiveNamePrefix = BORG_ARCHIVE_PREFIX_DEFAULT;
	
	private Integer serverId;
	
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

	@Override
	public String toListRepresentation(String... fields) {
		return null;
	}

	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}
	
	public String getLocalBackupCron() {
		return localBackupCron;
	}

	public void setLocalBackupCron(String localBackupCron) {
		this.localBackupCron = localBackupCron;
	}

	public String getLocalBackupPruneCron() {
		return localBackupPruneCron;
	}

	public void setLocalBackupPruneCron(String localBackupPruneCron) {
		this.localBackupPruneCron = localBackupPruneCron;
	}

	public static class BorgDescriptionBuilder {
		private final Integer serverId;
		private String repo = BORG_REPO_DEFAULT;
		private Set<String> includes = new HashSet<>();
		private Set<String> excludes = new HashSet<>();
		private String archiveFormat = BORG_ARCHIVE_FORMAT_DEFAULT;
		private String archiveCron;
		private String pruneCron;
		private String archiveNamePrefix = BORG_ARCHIVE_PREFIX_DEFAULT;
		
		public BorgDescriptionBuilder(int serverId) {
			this.serverId = serverId;
		}
		
		
		public BorgDescriptionBuilder addInclude(String include) {
			this.includes.add(include);
			return this;
		}
		
		public BorgDescriptionBuilder addExclude(String exclude) {
			this.excludes.add(exclude);
			return this;
		}
		
		public BorgDescriptionBuilder withArchiveCron(String archiveCron) {
			this.archiveCron = archiveCron;
			return this;
		}

		
		public BorgDescriptionBuilder withPruneCron(String pruneCron) {
			this.pruneCron = pruneCron;
			return this;
		}
		
		
		public BorgDescription build() {
			BorgDescription bd = new BorgDescription();
			bd.setArchiveCron(archiveCron);
			bd.setArchiveFormat(archiveFormat);
			bd.setArchiveNamePrefix(archiveNamePrefix);
			bd.setCreatedAt(new Date());
			bd.setExcludes(new ArrayList<>(excludes));
			bd.setIncludes(new ArrayList<>(includes));
			bd.setPruneCron(pruneCron);
			bd.setRepo(repo);
			bd.setServerId(serverId);
			return bd;
		}

	}
}
