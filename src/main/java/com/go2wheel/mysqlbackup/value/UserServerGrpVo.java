package com.go2wheel.mysqlbackup.value;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class UserServerGrpVo implements ToListRepresentation {
	
	private UserAccount userAccount;
	private ServerGrp serverGrp;
	
	private String cron;
	
	private Integer id;
	
	public UserServerGrpVo(int usgId, UserAccount userAccount, ServerGrp serverGrp, String cron) {
		this.userAccount = userAccount;
		this.serverGrp = serverGrp;
		this.setId(usgId);
		this.setCron(cron);
	}
	public UserAccount getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}
	public ServerGrp getServerGrp() {
		return serverGrp;
	}
	public void setServerGrp(ServerGrp serverGrp) {
		this.serverGrp = serverGrp;
	}
	
	@Override
	public String toListRepresentation(String... fields) {
		return "id: " + id + ", cron: " + getCron()  + ", "  + ObjectUtil.toListRepresentation(userAccount,"id", "name") + ", " + ObjectUtil.toListRepresentation(serverGrp,"id", "ename");
	}
	
	@Override
	public String toString() {
		return toListRepresentation();
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
}
