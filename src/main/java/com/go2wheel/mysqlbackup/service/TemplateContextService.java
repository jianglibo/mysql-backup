package com.go2wheel.mysqlbackup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.mail.ServerContext;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.value.Server;
import com.go2wheel.mysqlbackup.value.ServerGrp;

@Service
public class TemplateContextService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserGroupLoader userGroupLoader;

	public ServerGroupContext createMailerContext(Subscribe subscribe) throws ExecutionException {
		ServerGrp sg = userGroupLoader.getGroupByName(subscribe.getGroupname());
		UserAccount ua = userGroupLoader.getUserByName(subscribe.getUsername());

		List<Server> servers = sg.getServers();

		List<ServerContext> oscs = new ArrayList<>();

		for (Server server : servers) {
			ServerContext osc = prepareServerContext(server);
			oscs.add(osc);
		}
		return new ServerGroupContext(oscs,new ArrayList<>(), ua, sg, null);

	}
	
//	public ServerGroupContext createMailerContext(int subscribeId) {
//		Subscribe subscribe = userServerGrpDbService.findById(subscribeId);
//		return createMailerContext(subscribe);
//	}
	
	public ServerContext prepareServerContext(Server server) {
//		List<ServerState> serverStates = serverStateDbService.getItemsInDays(server, dvs.getServerStateCount());
//		List<MysqlFlush> mysqlFlushs = mysqlFlushDbService.getRecentItems(server, dvs.getFlushCount());
//		List<StorageState> storageStates = storageStateDbService.getItemsInDays(server, dvs.getStorageStateCount());
//		List<MysqlDump> mysqlDumps = mysqlDumpDbService.getRecentItems(server, dvs.getMysqlDumpCount());
//		List<BorgDownload> borgDownloads = borgDownloadDbService.getRecentItems(server, dvs.getBorgDownloadCount());
//		ServerContext osc = new ServerContext(serverStates, mysqlFlushs, storageStates, mysqlDumps,
//				borgDownloads);
//		osc.setServer(server);
//		return osc;
		return null;
	}
	

}
