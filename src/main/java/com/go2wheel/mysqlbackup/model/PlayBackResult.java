package com.go2wheel.mysqlbackup.model;

/**
 * 
 * @author jianglibo@gmail.com
 *
 */
public class PlayBackResult extends BaseModel {
	
	private Integer playBackId;
	
	private boolean success;
	
	private String reason;
	
	public Integer getPlayBackId() {
		return playBackId;
	}

	public void setPlayBackId(Integer playBackId) {
		this.playBackId = playBackId;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public String toListRepresentation(String... fields) {
		if (fields.length == 0) {
			fields = new String[] {"id", "playBackId", "success"};
		}
		return super.toListRepresentation(fields);
	}

}
