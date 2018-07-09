package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class MysqlFlush extends BaseModel {
	
	private Integer serverId;
	/* 全部log文件的长度 */
	private Long fileSize;
	
	/*log文件的数量*/
	private Integer fileNumber;

	private Long timeCost;
	
	
	public Integer getServerId() {
		return serverId;
	}
	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public Long getTimeCost() {
		return timeCost;
	}
	public void setTimeCost(Long timeCost) {
		this.timeCost = timeCost;
	}
	
	public Integer getFileNumber() {
		return fileNumber;
	}
	public void setFileNumber(Integer fileNumber) {
		this.fileNumber = fileNumber;
	}
	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, fields);
	}

}
