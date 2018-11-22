package com.go2wheel.mysqlbackup.value;

public class BorgConfigFile extends ConfigFile {
	
	public String getArchiveCmd() {
		return getTaskcmd().get("archive");
	}
	
	public String getPruneCmd() {
		return getTaskcmd().get("prune");
	}

	public String getDownloadCmd() {
		return getTaskcmd().get("download");
	}
	
	public String getArchiveCron() {
		return getCrons().get("archive");
	}

	public String getPruneCron() {
		return getCrons().get("prune");
	}
}
