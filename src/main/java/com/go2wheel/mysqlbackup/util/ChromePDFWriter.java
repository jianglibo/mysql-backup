package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.dbservice.KeyValueDbService;
import com.go2wheel.mysqlbackup.model.KeyValue;

@Service
public class ChromePDFWriter {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String CHROME_EXECUTABLE_KEY = "db.chrome.executable";
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	@Autowired
	private KeyValueDbService keyValueDbService;

//	"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
	
	public Path writePdf(String url) {
		String chrome = settingsInDb.getString(CHROME_EXECUTABLE_KEY);
		if (!StringUtil.hasAnyNonBlankWord(chrome)) {
			logger.error("Please set db.chrome.executable value.");
			Path p = Paths.get("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
			if (Files.exists(p)) {
				KeyValue kv = keyValueDbService.findOneByKey(CHROME_EXECUTABLE_KEY);
				kv.setItemValue(p.toAbsolutePath().toString());
				keyValueDbService.save(kv);
				chrome = p.toAbsolutePath().toString();
			} else {
				return null;
			}
		}
		Path p = null;
		try {
			p = Files.createTempFile("chrome-headless", ".pdf");
			ProcessBuilder pb = new ProcessBuilder(chrome, "--headless", "--disable-gpu", "--print-to-pdf=" + p.toAbsolutePath().toString(), url);
			Process process = pb.start();
			process.waitFor();
		} catch (IOException | InterruptedException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
		return p;
	}
	
}
