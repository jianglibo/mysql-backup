package com.go2wheel.mysqlbackup.installer;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class BorgInstallInfo implements InstallInfo {

	private boolean installed;
	private String executable;
	private String version;

	@Override
	public String toString() {
		return ObjectUtil.dumpObjectAsMap(this);
	}

	public static BorgInstallInfo notInstalled() {
		BorgInstallInfo ii = new BorgInstallInfo();
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
