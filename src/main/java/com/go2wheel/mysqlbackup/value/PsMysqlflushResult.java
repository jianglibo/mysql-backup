package com.go2wheel.mysqlbackup.value;

import java.util.List;

public class PsMysqlflushResult  extends PsLogBase {
	
	private List<PsDownloadItem> result;

	public List<PsDownloadItem> getResult() {
		return result;
	}

	public void setResult(List<PsDownloadItem> result) {
		this.result = result;
	}


}
