package com.go2wheel.mysqlbackup.installer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.JSchException;

public interface Installer<I extends InstallInfo> {

	FacadeResult<I> install(Server server, Software software, Map<String, String> parasMap) throws JSchException;
	FacadeResult<I> uninstall(Server server, Software software) throws JSchException;
	
	CompletableFuture<AsyncTaskValue> installAsync(Server server, Software software, String msgkey, Map<String, String> parasMap);
	CompletableFuture<AsyncTaskValue> uninstallAsync(Server server, Software software, String msgkey);
	
	boolean canHandle(Software software);
	
	void syncToDb();
}
