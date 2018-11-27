package com.go2wheel.mysqlbackup.model;

public class PsMysqldumpResult  extends PsLogBase {
	
	private PsDownloadItem result;

	public PsDownloadItem getResult() {
		return result;
	}

	public void setResult(PsDownloadItem result) {
		this.result = result;
	}

}
