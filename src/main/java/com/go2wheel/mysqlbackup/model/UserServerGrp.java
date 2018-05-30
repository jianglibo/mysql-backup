package com.go2wheel.mysqlbackup.model;

import java.util.Date;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class UserServerGrp extends BaseModel {
	
	private Integer userAccountId;
	private Integer serverGrpId;
	private String cronExpression;
	private Date createdAt;
	
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
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	public static class UserServerGrpBuilder {
		
		private final Integer userAccountId;
		private final Integer serverGrpId;
		private String cronExpression;
		
		public UserServerGrpBuilder(Integer userAccountId, Integer serverGrpId) {
			super();
			this.userAccountId = userAccountId;
			this.serverGrpId = serverGrpId;
		}
		
		public UserServerGrpBuilder withCronExpression(String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}
		
		public UserServerGrp build() {
			UserServerGrp usg = new UserServerGrp();
			usg.setCreatedAt(new Date());
			usg.setCronExpression(cronExpression);
			usg.setServerGrpId(serverGrpId);
			usg.setUserAccountId(userAccountId);
			return usg;
		}
		
		
		
		
		
	}

	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, "userAccountId", "serverGrpId");
	}
}
