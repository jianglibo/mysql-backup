package com.go2wheel.mysqlbackup.borg;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.jcraft.jsch.Session;

@Component
public class BorgInstaller {
	
	public static final String BORG_BINARY = "borg-linux64";
	public static final String REMOTE_BORG_BINARY = "/usr/local/bin/borg";

	private MyAppSettings appSettings;
	
	public void uploadBinary(Session session) {
		Path localPath = appSettings.getDownloadRoot().resolve(BORG_BINARY);
		if (Files.exists(localPath)) {
			ScpUtil.to(session, localPath.toString(), REMOTE_BORG_BINARY);
		}
	}


	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
}
