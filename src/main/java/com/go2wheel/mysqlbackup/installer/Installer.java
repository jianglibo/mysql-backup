package com.go2wheel.mysqlbackup.installer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.value.FacadeResult;

public interface Installer<I extends InstallInfo> {

	FacadeResult<I> install(Server server, Software software, Map<String, String> parasMap);
	FacadeResult<I> uninstall(Server server, Software software);
	
	CompletableFuture<FacadeResult<I>> installAsync(Server server, Software software, Map<String, String> parasMap);
	CompletableFuture<FacadeResult<I>> uninstallAsync(Server server, Software software);
	
	boolean canHandle(Software software);
	
	void syncToDb();
}
