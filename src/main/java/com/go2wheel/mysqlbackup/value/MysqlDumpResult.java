package com.go2wheel.mysqlbackup.value;

public class MysqlDumpResult {
	
	private boolean success;
	
	private LinuxLsl fi;
	
	private String reason;
	
	private MysqlDumpResult() {}
	
	public static MysqlDumpResult successResult(LinuxLsl fi) {
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

	public LinuxLsl getFi() {
		return fi;
	}

	public void setFi(LinuxLsl fi) {
		this.fi = fi;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
