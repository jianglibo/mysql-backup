package com.go2wheel.mysqlbackup.value;

import java.util.List;

public class PsMysqlflushResult  extends PsLogBase {
	
	private List<PsDownloadItem> result;
	
	private PsDownloadCatalog download;

	public List<PsDownloadItem> getResult() {
		return result;
	}

	public void setResult(List<PsDownloadItem> result) {
		this.result = result;
	}

	public PsDownloadCatalog getDownload() {
		return download;
	}

	public void setDownload(PsDownloadCatalog download) {
		this.download = download;
	}
}
