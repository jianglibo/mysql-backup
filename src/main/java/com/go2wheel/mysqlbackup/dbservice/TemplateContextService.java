package com.go2wheel.mysqlbackup.dbservice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.mail.ServerContext;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.model.UserAccount;

@Service
public class TemplateContextService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServerStateDbService serverStateDbService;

	@Autowired
	private SubscribeDbService userServerGrpDbService;

	@Autowired
	private UserAccountDbService userAccountDbService;

	@Autowired
	private ServerGrpDbService serverGrpDbService;
	
	@Autowired
	private JobLogDbService jobLogDbService;

	@Autowired
	private ServerDbService serverDbService;
	
//	@Autowired
//	private MysqlFlushDbService mysqlFlushDbService;

	@Autowired
	private StorageStateDbService storageStateDbService;

//	@Autowired
//	private MysqlDumpDbService mysqlDumpDbService;

	@Autowired
	private BorgDownloadDbService borgDownloadDbService;
	
	public ServerGroupContext createMailerContext(Subscribe subscribe) {
		ServerGrp sg = serverGrpDbService.findById(subscribe.getServerGrpId());
		UserAccount ua = userAccountDbService.findById(subscribe.getUserAccountId());

		List<Server> servers = serverGrpDbService.getServers(sg).stream().map(sv -> serverDbService.loadFull(sv))
				.collect(Collectors.toList());

		List<ServerContext> oscs = new ArrayList<>();

		for (Server server : servers) {
			ServerContext osc = prepareServerContext(server);
			oscs.add(osc);
		}
		Server myself = serverDbService.findByHost("localhost");
//		List<JobLog> jobLogs = jobLogDbService.getRecentItems(dvs.getDefaultCount().getInteger(DefaultValues.JOB_LOG_CN));
		return new ServerGroupContext(oscs,new ArrayList<>(), ua, sg, prepareServerContext(myself));

	}
	
	public ServerGroupContext createMailerContext(int subscribeId) {
		Subscribe subscribe = userServerGrpDbService.findById(subscribeId);
		return createMailerContext(subscribe);
	}
	
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
