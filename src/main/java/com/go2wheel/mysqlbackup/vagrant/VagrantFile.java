package com.go2wheel.mysqlbackup.vagrant;

import java.util.List;

public class VagrantFile {
	
	public static final String VAGRANT_FILE_NAME = "Vagrantfile";
	
	private List<String> lines;
	
	public VagrantFile(List<String> lines) {
		this.lines = lines;
	}
	
	
	
	

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
	}

}
