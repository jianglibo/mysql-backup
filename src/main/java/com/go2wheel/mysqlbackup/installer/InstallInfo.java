package com.go2wheel.mysqlbackup.installer;

public interface InstallInfo {
	
	boolean isInstalled(); 
	String getExecutable();
	String getVersion();
}
