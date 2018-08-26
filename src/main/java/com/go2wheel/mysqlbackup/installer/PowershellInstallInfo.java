package com.go2wheel.mysqlbackup.installer;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class PowershellInstallInfo implements InstallInfo {

	private boolean installed;
	private String executable;
	private String version;

	@Override
	public String toString() {
		return ObjectUtil.dumpObjectAsMap(this);
	}

	public static PowershellInstallInfo notInstalled() {
		PowershellInstallInfo ii = new PowershellInstallInfo();
		ii.setInstalled(false);
		return ii;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public String getExecutable() {
		return executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
