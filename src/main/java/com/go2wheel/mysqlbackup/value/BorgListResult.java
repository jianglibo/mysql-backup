package com.go2wheel.mysqlbackup.value;

import java.util.ArrayList;
import java.util.List;

public class BorgListResult {
	
	private List<String> archives = new ArrayList<>();
	
	public BorgListResult(RemoteCommandResult rcr) {
		if (rcr.getExitValue() == 0) {
			this.setArchives(rcr.getAllTrimedNotEmptyLines());
		}
	}

	public List<String> getArchives() {
		return archives;
	}

	public void setArchives(List<String> archives) {
		this.archives = archives;
	}

}
