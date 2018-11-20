package com.go2wheel.mysqlbackup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.MyAppSettings;

@Service
public class BorgService {

	@Autowired
	private MyAppSettings myAppSettings;
	
	public BorgRunner newBorgRunner(String configurationFile) {
		return new BorgRunner(configurationFile);
	}
	
	
	public class BorgRunner {
		
		private String configurationFile;
		
		public BorgRunner(String configurationFile) {
			this.configurationFile = configurationFile;
		}
		
		public String archive() {
			return null;
		}
		
		
	}

}
