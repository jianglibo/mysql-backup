package com.go2wheel.mysqlbackup.vagrant;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;

@Component
public class VagrantFileUtil {

	private MyAppSettings appSettings;
	
	public VagrantFile loadVagrantFile() throws IOException {
		Path p = appSettings.getDataRoot().getParent().resolve(VagrantFile.VAGRANT_FILE_NAME);
		if (!Files.exists(p)) {
			InputStream is = ClassLoader.class.getResourceAsStream("/" + VagrantFile.VAGRANT_FILE_NAME);
			Files.copy(is, p);
		}
		return new VagrantFile(Files.readAllLines(p));
	}



	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
	
	
}
