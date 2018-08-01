package com.go2wheel.mysqlbackup.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BorgListResult {

	private List<String> archives = new ArrayList<>();

	public BorgListResult(RemoteCommandResult rcr) {
		if (rcr.getExitValue() == 0) {
			this.setArchives(rcr.getAllTrimedNotEmptyLines());
		}
	}

	public List<String> getArchiveNames() {
		List<String> ans = getArchives().stream().map(line -> line.split("\\s+", 2)).filter(ll -> ll.length == 2)
				.map(ll -> ll[0].trim()).collect(Collectors.toList());
		Collections.reverse(ans);
		return ans;
	}

	public List<String> getArchives() {
		return archives;
	}

	public void setArchives(List<String> archives) {
		this.archives = archives;
	}

}
