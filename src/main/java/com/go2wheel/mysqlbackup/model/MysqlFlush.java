package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.value.ResultEnum;

public class MysqlFlush extends BaseModel {
	
	private Integer serverId;
	private Long fileSize;
	private Integer fileNumber;

	private ResultEnum result;
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

	public ResultEnum getResult() {
		return result;
	}
	public void setResult(ResultEnum result) {
		this.result = result;
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
		// TODO Auto-generated method stub
		return null;
	}

}
