package com.go2wheel.mysqlbackup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.mail.ServerContext;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.value.DefaultValues;

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
	
	@Autowired
	private DefaultValues dvs;

	@Autowired
	private MysqlFlushDbService mysqlFlushDbService;

	@Autowired
	private StorageStateDbService storageStateDbService;

	@Autowired
	private MysqlDumpDbService mysqlDumpDbService;

	@Autowired
	private BorgDownloadDbService borgDownloadDbService;
	
	public ServerGroupContext createMailerContext(Subscribe userServerGrp) {
		ServerGrp sg = serverGrpDbService.findById(userServerGrp.getServerGrpId());
		UserAccount ua = userAccountDbService.findById(userServerGrp.getUserAccountId());

		List<Server> servers = serverGrpDbService.getServers(sg).stream().map(sv -> serverDbService.loadFull(sv))
				.collect(Collectors.toList());

		List<ServerContext> oscs = new ArrayList<>();

		for (Server server : servers) {
			ServerContext osc = prepareServerContext(server);
			oscs.add(osc);
		}
		Server myself = serverDbService.findByHost("localhost");
		List<JobLog> jobLogs = jobLogDbService.getRecentItems(dvs.getDefaultCount().getInteger(DefaultValues.JOB_LOG_CN));
		return new ServerGroupContext(oscs,jobLogs, ua, sg, prepareServerContext(myself));

	}
	
	public ServerGroupContext createMailerContext(int userServerGrpId) {
		Subscribe usg = userServerGrpDbService.findById(userServerGrpId);
		return createMailerContext(usg);
	}
	
	public ServerContext prepareServerContext(Server server) {
		List<ServerState> serverStates = serverStateDbService.getItemsInDays(server, dvs.getDefaultCount().getInteger(DefaultValues.SERVER_STATE_CN));
		List<MysqlFlush> mysqlFlushs = mysqlFlushDbService.getRecentItems(server, dvs.getDefaultCount().getInteger(DefaultValues.MYSQL_FLUSH_CN));
		List<StorageState> storageStates = storageStateDbService.getItemsInDays(server, dvs.getDefaultCount().getInteger(DefaultValues.STORAGE_STATE_CN));
		List<MysqlDump> mysqlDumps = mysqlDumpDbService.getRecentItems(server, dvs.getDefaultCount().getInteger(DefaultValues.MYSQL_DUMP_CN));
		List<BorgDownload> borgDownloads = borgDownloadDbService.getRecentItems(server, dvs.getDefaultCount().getInteger(DefaultValues.BORG_DOWNLOAD_CN));
		ServerContext osc = new ServerContext(serverStates, mysqlFlushs, storageStates, mysqlDumps,
				borgDownloads);
		osc.setServer(server);
		return osc;
	}
	

}
