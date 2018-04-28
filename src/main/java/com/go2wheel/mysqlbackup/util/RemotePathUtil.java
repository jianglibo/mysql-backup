package com.go2wheel.mysqlbackup.util;

import com.go2wheel.mysqlbackup.value.Box;

public class RemotePathUtil {
	
	public static String getParentWithEndingSlash(String remotePath) {
		return remotePath.substring(0, remotePath.lastIndexOf('/') + 1);
	}
	
	public static String getLogBinFile(Box box, String onlyFilename) {
		String remoteDir = box.getMysqlInstance().getLogBinSetting().getLogBinDirWithEndingSlash();
		return remoteDir + onlyFilename;
	}

}
