package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.model.Server;
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
	
	private ApplicationState applicationState;

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
	
	public void writeDescription(Server server) throws IOException {
		Path dstFile = null;
		String ds = YamlInstance.INSTANCE.yaml.dumpAsMap(server);
		Path dstDir = appSettings.getDataRoot().resolve(server.getHost());
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		dstFile = dstDir.resolve(BackupCommand.DESCRIPTION_FILENAME);
		FileUtil.atomicWriteFile(dstFile, ds.getBytes());

	}


//	public FacadeResult<Box> serverCreate(String host) {
//		Box box = null;
//		try {
//			if (Files.exists(appSettings.getDataRoot().resolve(host))) {
//				box = applicationState.getServerByHost(host);
//				if (box != null) {
//					return FacadeResult.doneExpectedResult(box, CommonActionResult.PREVIOUSLY_DONE);
//				}
//			}
//			box = new Box();
//			box.setHost(host);
//			writeDescription(box);
//			return FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
//		} catch (IOException e) {
//			ExceptionUtil.logErrorException(logger, e);
//			return FacadeResult.unexpectedResult(e);
//		}
//	}

	@Autowired
	public void setApplicationState(ApplicationState applicationState) {
		this.applicationState = applicationState;
	}

	public FacadeResult<Box> serverCreate(String host) {
		// TODO Auto-generated method stub
		return null;
	}
}
