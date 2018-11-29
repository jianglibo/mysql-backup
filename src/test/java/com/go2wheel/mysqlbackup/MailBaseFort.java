package com.go2wheel.mysqlbackup;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.service.ConfigFileLoader;
import com.go2wheel.mysqlbackup.service.UserGroupLoader;

public class MailBaseFort extends SpringBaseFort {
	
	@Autowired
	protected UserGroupLoader userGroupLoader;
	
	@Autowired
	protected ConfigFileLoader configFileLoader;

	private Path psconfig = Paths.get("fixtures", "psconfigs", "config-templates");
	protected Path psappconfig = Paths.get("fixtures", "psconfigs", "psappconfigs");

	protected Path[] getPathes() {
		return new Path[] { psconfig.resolve(MyAppSettings.SERVER_GROUP_FILE_NAME),
				psconfig.resolve(MyAppSettings.USER_FILE_NAME), psconfig.resolve(MyAppSettings.SUBSCRIBE_FILE_NAME),
				psconfig.resolve(MyAppSettings.ADMIN_FILE_NAME) };
	}

}
