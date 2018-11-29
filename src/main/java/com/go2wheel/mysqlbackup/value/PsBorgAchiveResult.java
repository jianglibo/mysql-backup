package com.go2wheel.mysqlbackup.value;

import java.util.Map;

public class PsBorgAchiveResult  extends PsLogBase {
	
	private Map<String, Object> result;
	
	private PsDownloadResult download;

	public Map<String, Object> getResult() {
		return result;
	}

	public void setResult(Map<String, Object> result) {
		this.result = result;
	}

	public PsDownloadResult getDownload() {
		return download;
	}

	public void setDownload(PsDownloadResult download) {
		this.download = download;
	}
}
