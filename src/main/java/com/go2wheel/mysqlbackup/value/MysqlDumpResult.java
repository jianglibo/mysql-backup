package com.go2wheel.mysqlbackup.value;

import com.go2wheel.mysqlbackup.util.StringUtil.LinuxFileInfo;

public class MysqlDumpResult {
	
	private boolean success;
	
	private LinuxFileInfo fi;
	
	private String reason;
	
	private MysqlDumpResult() {}
	
	public static MysqlDumpResult successResult(LinuxFileInfo fi) {
		MysqlDumpResult mdr = new MysqlDumpResult();
		mdr.setSuccess(true);
		mdr.setFi(fi);
		return mdr;
	}
	
	public static MysqlDumpResult failedResult(String reason) {
		MysqlDumpResult mdr = new MysqlDumpResult();
		mdr.setSuccess(false);
		mdr.setReason(reason);
		return mdr;
	}

	
	@Override
	public String toString() {
		if (success) {
			return String.format("[成功: %s, 文件: %s, 长度: %s",success, fi.getFilename(), fi.getSize());
		} else {
			return String.format("[成功: %s, 原因: %s",success, reason);
		}
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public LinuxFileInfo getFi() {
		return fi;
	}

	public void setFi(LinuxFileInfo fi) {
		this.fi = fi;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
