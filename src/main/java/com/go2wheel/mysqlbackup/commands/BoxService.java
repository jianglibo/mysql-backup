package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.FileUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

@Service
public class BoxService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private MyAppSettings appSettings;

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
	
	public void writeDescription(Box box) throws IOException {
		Path dstFile = null;
		String ds = YamlInstance.INSTANCE.yaml.dumpAsMap(box);
		Path dstDir = appSettings.getDataRoot().resolve(box.getHost());
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		dstFile = dstDir.resolve(BackupCommand.DESCRIPTION_FILENAME);
		FileUtil.atomicWriteFile(dstFile, ds.getBytes());

	}


	public FacadeResult<Box> serverCreate(String host) {
		try {
			if (Files.exists(appSettings.getDataRoot().resolve(host))) {
				return FacadeResult.doneExpectedResult(CommonActionResult.PREVIOUSLY_DONE);
			}
			Box box = new Box();
			box.setHost(host);
			writeDescription(box);
			return FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
		} catch (IOException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
}
