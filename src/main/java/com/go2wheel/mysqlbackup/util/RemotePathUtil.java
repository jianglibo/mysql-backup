package com.go2wheel.mysqlbackup.util;

import com.go2wheel.mysqlbackup.model.Server;

public class RemotePathUtil {
	
	public static String getParentWithEndingSlash(String remotePath) {
		return remotePath.substring(0, remotePath.lastIndexOf('/') + 1);
	}
	
	public static String getLogBinFile(Server server, String onlyFilename) {
		String remoteDir = server.getMysqlInstance().getLogBinSetting().getLogBinDirWithEndingSlash();
		return remoteDir + onlyFilename;
	}

}
