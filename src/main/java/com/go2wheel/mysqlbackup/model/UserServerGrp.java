package com.go2wheel.mysqlbackup.model;

import java.util.Date;

import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;

public class UserServerGrp extends BaseModel {
	
	private Integer userAccountId;
	private Integer serverGrpId;
	
	private String name;
	
	@CronExpressionConstraint
	private String cronExpression;
	
	public Integer getUserAccountId() {
		return userAccountId;
	}
	public void setUserAccountId(Integer userAccountId) {
		this.userAccountId = userAccountId;
	}
	public Integer getServerGrpId() {
		return serverGrpId;
	}
	public void setServerGrpId(Integer serverGrpId) {
		this.serverGrpId = serverGrpId;
	}
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	
	
	
	public static class UserServerGrpBuilder {
		
		private final Integer userAccountId;
		private final Integer serverGrpId;
		private final String cronExpression;
		
		private String name;
		
		public UserServerGrpBuilder(Integer userAccountId, Integer serverGrpId, String cronExpression) {
			super();
			this.userAccountId = userAccountId;
			this.serverGrpId = serverGrpId;
			this.cronExpression = cronExpression;
		}
		
		public UserServerGrpBuilder withName(String name) {
			this.name = name;
			return this;
		}
		
		
		public UserServerGrp build() {
			UserServerGrp usg = new UserServerGrp();
			usg.setCreatedAt(new Date());
			usg.setCronExpression(cronExpression);
			usg.setServerGrpId(serverGrpId);
			usg.setUserAccountId(userAccountId);
			usg.setName(name);
			return usg;
		}
	}

	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, "name", "userAccountId", "serverGrpId");
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
