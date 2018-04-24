package com.go2wheel.mysqlbackup.mysqlinstaller;

import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.jcraft.jsch.Session;

public class MySqlInstaller {
	
	private FileDownloader fileDownloader;
	
	public MySqlInstaller(FileDownloader fileDownloader) {
		this.fileDownloader = fileDownloader;
	}
	
	public void install(Session sshSession, String twoDigitVersion) {
		
		
	}

}
