package com.go2wheel.mysqlbackup.vo;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.value.ToListRepresentation;

public class UserServerGrpVo implements ToListRepresentation {
	
	private UserAccount userAccount;
	private ServerGrp serverGrp;
	
	private Integer id;
	
	public UserServerGrpVo(int usgId, UserAccount userAccount, ServerGrp serverGrp) {
		this.userAccount = userAccount;
		this.serverGrp = serverGrp;
		this.setId(usgId);
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
		return "id: " + id + ", " + ObjectUtil.toListRepresentation(userAccount,"id", "name") + ", " + ObjectUtil.toListRepresentation(serverGrp,"id", "ename");
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
}
