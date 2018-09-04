package com.go2wheel.mysqlbackup.model;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

import com.go2wheel.mysqlbackup.validator.BackupPruneStrategyConstraint;
import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class RobocopyDescription extends BaseModel {

	public static final String ROBO_LOCAL_BACKUP_PRUNE_STRATEGY = "0 0 2 7 4 1 1";
	
	@NotEmpty
	private String repo;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String invokeCron;
	
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
	
	public String workingSpaceEndWithSlash() {
		String repoSlash = getRepo().replace('\\', '/');
		if (!repoSlash.endsWith("/")) {
			repoSlash += "/";
		}
		return repoSlash + "workingspace/";
	}
	
	public String appendToRepo(String relative) {
		String repoSlash = getRepo().replace('\\', '/');
		String relativeSlash = relative.replace('\\', '/');
		if (!repoSlash.endsWith("/")) {
			repoSlash += "/";
		}
		
		if (relativeSlash.startsWith("/")) {
			relativeSlash = relativeSlash.substring(1);
		}
		
		return repoSlash + "archive/" + relativeSlash;
	}


	public String getInvokeCron() {
		return invokeCron;
	}

	public void setInvokeCron(String invokeCron) {
		this.invokeCron = invokeCron;
	}

	public void setRepo(String repo) {
		this.repo = repo;
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
		private String invokeCron;
		private String localBackupCron;
		
		private String pruneStrategy = ROBO_LOCAL_BACKUP_PRUNE_STRATEGY;
		
		public RobocopyDescriptionBuilder(int serverId) {
			this.serverId = serverId;
		}
		
		public RobocopyDescriptionBuilder withInvokeCron(String invokeCron) {
			this.invokeCron = invokeCron;
			return this;
		}

		
		public RobocopyDescriptionBuilder withLocalBackupCron(String localBackupCron) {
			this.localBackupCron = localBackupCron;
			return this;
		}
		public RobocopyDescriptionBuilder withPruneStragegy(String pruneStrategy) {
			this.setPruneStrategy(pruneStrategy);
			return this;
		}	
		
		public RobocopyDescription build() {
			RobocopyDescription bd = new RobocopyDescription();
			bd.setInvokeCron(invokeCron);
			bd.setCreatedAt(new Date());
			bd.setLocalBackupCron(localBackupCron);
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
