package com.go2wheel.mysqlbackup.model;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
	
	@NotNull
	private Integer serverId;
	
	private String compressCommand;
	
	private String expandCommand;
	
	private boolean alwaysFullBackup;

	@NotEmpty
	@Pattern(regexp=".*\\.\\w{3}")
	private String archiveName;
	
	private List<RobocopyItem> robocopyItems;
	
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
	
	public String getWorkingSpaceCompressed(String...subs) {
		String[] ns = new String[subs.length + 1];
		ns[0] = "compressed";
		for(int i =0 ;i <subs.length;i++) {
			ns[i+1] = subs[i];
		}
		return abs("workingspace", ns);
	}
	
	public String getWorkingSpaceCompressedArchive() {
		return getWorkingSpaceCompressed(getArchiveName());
	}
	
	public String getWorkingSpaceExpanded() {
		return abs("workingspace", new String[] {"expanded"});
	}

	public String getWorkingSpaceScriptFile() {
		return abs("workingspace", new String[] {"robocopy.ps1"});
	}

	public String getWorkingSpaceRoboLog() {
		return abs("workingspace", new String[] {"robocopy.log"});
	}
	
	public String getWorkingSpaceAbsolute(String...subs) {
		return abs("workingspace", subs);
	}
	
	public String getRobocopyDst() {
		return abs("robocopydst", new String[] {});
	}
	
	public String getRobocopyItemDst(String itemName) {
		return abs("robocopydst", new String[] {itemName});
	}

	
	public String getRobocopyDstNoRoot(String...subs) {
		String s =  abs("robocopydst", subs);
		int i = s.indexOf('/');
		return s.substring(i + 1);
	}
	
	private String abs(String rootNameInRepo, String[] subs) {
		String baseSlash = getRepo().replace('\\', '/');
		if (!baseSlash.endsWith("/")) {
			baseSlash += "/";
		}
		if (subs.length == 0) {
			return baseSlash + rootNameInRepo;
		} else {
			return baseSlash + rootNameInRepo + "/" + String.join("/", subs);			
		}

	}

	public boolean isAlwaysFullBackup() {
		return alwaysFullBackup;
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

	public List<RobocopyItem> getRobocopyItems() {
		return robocopyItems;
	}

	public void setRobocopyItems(List<RobocopyItem> robocopyItems) {
		this.robocopyItems = robocopyItems;
	}

	public String getCompressCommand() {
		return compressCommand;
	}

	public void setCompressCommand(String compressCommand) {
		this.compressCommand = compressCommand;
	}

	public String getExpandCommand() {
		return expandCommand;
	}

	public void setExpandCommand(String expandCommand) {
		this.expandCommand = expandCommand;
	}

	public String getArchiveName() {
		return archiveName;
	}

	public void setArchiveName(String archiveName) {
		this.archiveName = archiveName;
	}



	public void setAlwaysFullBackup(boolean alwaysFullBackup) {
		this.alwaysFullBackup = alwaysFullBackup;
	}

	public static class RobocopyDescriptionBuilder {
		private final Integer serverId;
		private final String repo;
		private String invokeCron;
		private String localBackupCron;
		
		private String pruneStrategy = ROBO_LOCAL_BACKUP_PRUNE_STRATEGY;
		
		public RobocopyDescriptionBuilder(int serverId, String repo) {
			this.serverId = serverId;
			this.repo = repo;
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
