package com.go2wheel.mysqlbackup.vagrant;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.SettingsInDb;

@Component
public class VagrantFileUtil {

	private SettingsInDb settingsInDb;
	
	public VagrantFile loadVagrantFile() throws IOException {
		Path p = settingsInDb.getDataDir().getParent().resolve(VagrantFile.VAGRANT_FILE_NAME);
		if (!Files.exists(p)) {
			InputStream is = ClassLoader.class.getResourceAsStream("/" + VagrantFile.VAGRANT_FILE_NAME);
			Files.copy(is, p);
		}
		return new VagrantFile(Files.readAllLines(p));
	}



	@Autowired
	public void setAppSettings(SettingsInDb appSettings) {
		this.settingsInDb = appSettings;
	}
	
	
}
