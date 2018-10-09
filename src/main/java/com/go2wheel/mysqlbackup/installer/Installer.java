package com.go2wheel.mysqlbackup.installer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.JSchException;

public interface Installer<I extends InstallInfo> {

	FacadeResult<I> install(Server server, Software software, Map<String, String> parasMap) throws JSchException, IOException, UnExpectedInputException, UnExpectedOutputException;
	FacadeResult<I> uninstall(Server server, Software software) throws JSchException, IOException, UnExpectedInputException;
	
	CompletableFuture<AsyncTaskValue> installAsync(Server server, Software software, String msgkey,Long id, Map<String, String> parasMap);
	CompletableFuture<AsyncTaskValue> uninstallAsync(Server server, Software software, String msgkey, Long id);
	
	boolean canHandle(Software software);
	
	String getDescriptionMessageKey();
	
	void syncToDb();
}
