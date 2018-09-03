package com.go2wheel.mysqlbackup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.go2wheel.mysqlbackup.validator.BackupPruneStrategyConstraint;
import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class RobocopyDescription extends BaseModel {

	public static final String ROBO_LOCAL_BACKUP_PRUNE_STRATEGY = "0 0 2 7 4 1 1";
	
	private String repo;
	private List<String> includes = new ArrayList<>();
	private List<String> excludes = new ArrayList<>();
	
	@CronExpressionConstraint(allowEmpty=true)
	private String archiveCron;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String pruneCron;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String localBackupCron;
	
	@BackupPruneStrategyConstraint(allowEmpty=true)
	private String pruneStrategy = ROBO_LOCAL_BACKUP_PRUNE_STRATEGY;
	
	private Integer serverId;
	
	public RobocopyDescription() {
		super();
	}
	
	@Override
	public String toString() {
		return YamlInstance.INSTANCE.yaml.dumpAsMap(this);
	}
	
	public String getRepo() {
		return repo;
	}
	
	public String getArchiveCron() {
		return archiveCron;
	}

	public void setArchiveCron(String archiveCron) {
		this.archiveCron = archiveCron;
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

	public List<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}


	public String getPruneCron() {
		return pruneCron;
	}

	public void setPruneCron(String pruneCron) {
		this.pruneCron = pruneCron;
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

	public String getPruneStrategy() {
		return pruneStrategy;
	}

	public void setPruneStrategy(String pruneStrategy) {
		this.pruneStrategy = pruneStrategy;
	}

	public static class RobocopyDescriptionBuilder {
		private final Integer serverId;
		private String repo;
		private Set<String> includes = new HashSet<>();
		private Set<String> excludes = new HashSet<>();
		private String archiveCron;
		private String pruneCron;
		
		private String pruneStrategy = ROBO_LOCAL_BACKUP_PRUNE_STRATEGY;
		
		public RobocopyDescriptionBuilder(int serverId) {
			this.serverId = serverId;
		}
		
		public RobocopyDescriptionBuilder addInclude(String include) {
			this.includes.add(include);
			return this;
		}
		
		public RobocopyDescriptionBuilder addExclude(String exclude) {
			this.excludes.add(exclude);
			return this;
		}
		
		public RobocopyDescriptionBuilder withArchiveCron(String archiveCron) {
			this.archiveCron = archiveCron;
			return this;
		}

		
		public RobocopyDescriptionBuilder withPruneCron(String pruneCron) {
			this.pruneCron = pruneCron;
			return this;
		}
		public RobocopyDescriptionBuilder withPruneStragegy(String pruneStrategy) {
			this.setPruneStrategy(pruneStrategy);
			return this;
		}	
		
		public RobocopyDescription build() {
			RobocopyDescription bd = new RobocopyDescription();
			bd.setArchiveCron(archiveCron);
			bd.setCreatedAt(new Date());
			bd.setExcludes(new ArrayList<>(excludes));
			bd.setIncludes(new ArrayList<>(includes));
			bd.setPruneCron(pruneCron);
			bd.setRepo(repo);
			bd.setServerId(serverId);
			bd.setPruneStrategy(pruneStrategy);
			return bd;
		}

		public String getPruneStrategy() {
			return pruneStrategy;
		}

		public void setPruneStrategy(String pruneStrategy) {
			this.pruneStrategy = pruneStrategy;
		}

	}
}
