package com.go2wheel.mysqlbackup.mail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.util.SQLTimeUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.MsgKeyAndFills2;
import com.go2wheel.mysqlbackup.value.ServerStateAvg;

public class ServerContext {
	
	public static final String NORMAL_MESSAGE_KEY = "server.state.normal";
	public static final String DISK_FULL_MESSAGE_KEY = "server.state.diskfull";
	public static final String MEMROY_FULL_MESSAGE_KEY = "server.state.memoryfull";
	public static final String HEAVY_LOAD_MESSAGE_KEY = "server.state.heavyload";

	private List<ServerState> serverStates;
	private List<MysqlFlush> mysqlFlushs;
	private List<StorageState> storageStates;

	private List<MysqlDump> mysqlDumps;
	private List<BorgDownload> borgDownloads;

	private Server server;
	private String mem;

	public ServerContext() {
	}

	/**
	 * The numbers of parameter lists can be configurated db settings.
	 * @param serverStates
	 * @param mysqlFlushs
	 * @param storageStates
	 * @param mysqlDumps
	 * @param borgDownloads
	 */
	public ServerContext(List<ServerState> serverStates, List<MysqlFlush> mysqlFlushs, List<StorageState> storageStates,
			List<MysqlDump> mysqlDumps, List<BorgDownload> borgDownloads) {
		super();
		this.mysqlFlushs = mysqlFlushs;
		this.mysqlDumps = mysqlDumps;
		this.borgDownloads = borgDownloads;
		this.serverStates = serverStates;
		this.storageStates = storageStates;
		createMem();
	}
	
	public MsgKeyAndFills2 healthy() {
		if (getServerStates() != null) {
			OptionalDouble od = getServerStates().stream().mapToInt(one -> one.getAverageLoad()).average();
			double avd = od.isPresent() ? od.getAsDouble() : 0d;
			if (avd > server.getLoadValve()) {
				return new MsgKeyAndFills2(HEAVY_LOAD_MESSAGE_KEY, avd, server.getLoadValve());
			}
			od = getServerStates().stream().mapToInt(one -> one.memPercent()).average();
			avd = od.isPresent() ? od.getAsDouble() : 0d;
			if (avd > server.getMemoryValve()) {
				return new MsgKeyAndFills2(MEMROY_FULL_MESSAGE_KEY, avd, server.getMemoryValve());
			}
		}
		
		if (getStorageStates() != null) {
			OptionalDouble od = getStorageStates().stream().mapToDouble(one -> one.getUsedRatio()).max();
			double avd = od.isPresent() ? od.getAsDouble() : 0d;
			if (avd > server.getDiskValve()) {
				return new MsgKeyAndFills2(DISK_FULL_MESSAGE_KEY, avd, server.getDiskValve());
			}
		}
		return new MsgKeyAndFills2(NORMAL_MESSAGE_KEY);
	}

	/**
	 * The date as key. root and percent as a pair.
	 * @return
	 */
	public Map<String, Map<String, StorageState>> getStorageStateByDate() {
		Map<String, Set<StorageState>> stss = getStorageStates().stream().collect(Collectors.groupingBy(sst -> {
			return SQLTimeUtil.formatDate(sst.getCreatedAt());
		}, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));

		Map<String, Map<String, StorageState>> byDateAndRoot = new TreeMap<>();

		for (Map.Entry<String, Set<StorageState>> es : stss.entrySet()) {
			String dateKey = es.getKey();
			
			Set<StorageState> dailyData = es.getValue(); // date as the key.
			
			// root as key, maybe has multiple values, if we run program twice a day.
			Map<String, Set<StorageState>> oneByRoot = dailyData.stream().collect(Collectors.groupingBy(
					StorageState::getRoot, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));
			
			// get one record a day. If there was more than one record discard it.
			Map<String, StorageState> onlyOneRecord = new HashMap<>();
			
			for(Map.Entry<String, Set<StorageState>> oneRootItem : oneByRoot.entrySet()) {
				onlyOneRecord.put(oneRootItem.getKey(), oneRootItem.getValue().iterator().next());
			}
			
			byDateAndRoot.put(dateKey, onlyOneRecord);
		}
		return byDateAndRoot;
	}
	
	/**
	 * mount root as key. date and percent pair as value. 
	 * @return
	 */
	public Map<String, Map<String, StorageState>> getStorageStateByRoot() {
		Map<String, Set<StorageState>> stss = getStorageStates().stream().collect(Collectors.groupingBy(sst -> {
			return sst.getRoot();
		}, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));

		Map<String, Map<String, StorageState>> byRootAndDate = new TreeMap<>();

		for (Map.Entry<String, Set<StorageState>> es : stss.entrySet()) {
			String rootKey = es.getKey();
			Set<StorageState> rootData = es.getValue(); // all storageStates about one root, span multiple days.
			
			// date as the key, maybe has multiple values, if we run program twice a day.
			Map<String, Set<StorageState>> oneByDate = rootData.stream().collect(Collectors.groupingBy(sst ->
					SQLTimeUtil.formatDate(sst.getCreatedAt()), TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));
			
			// get one record for one root in one day. If there was more than one record discard it.
			Map<String, StorageState> onlyOneRecord = new TreeMap<>();
			
			for(Map.Entry<String, Set<StorageState>> oneDateItem : oneByDate.entrySet()) {
				onlyOneRecord.put(oneDateItem.getKey() /*it's date.*/, oneDateItem.getValue().iterator().next());
			}
			
			byRootAndDate.put(rootKey, onlyOneRecord);
		}
		return byRootAndDate;
	}

	public Map<String, Map<String, ServerStateAvg>> getServerStatebyHours() {
		Map<String, Set<ServerState>> byDate = byDate(getServerStates());

		Map<String, Map<String, Set<ServerState>>> byHours = new TreeMap<>();
		DateFormat df = new SimpleDateFormat("HH");

		for (Map.Entry<String, Set<ServerState>> es : byDate.entrySet()) {
			if (es.getValue().size() < 4) continue;
			Map<String, Set<ServerState>> oneByDate = es.getValue().stream().collect(Collectors.groupingBy(sst -> {
				String k = df.format(sst.getCreatedAt());
				return k;
			}, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));
			byHours.put(es.getKey(), oneByDate);
		}
		Map<String, Map<String, ServerStateAvg>> byHoursAvg = new TreeMap<>();
		

		for (Map.Entry<String, Map<String, Set<ServerState>>> es : byHours.entrySet()) {
			String dateKey = es.getKey(); // key is date. 1982-01-12
			Map<String, Set<ServerState>> hourValue = es.getValue();

			Map<String, ServerStateAvg> hourAvg = new TreeMap<>();

			for (Map.Entry<String, Set<ServerState>> hes : hourValue.entrySet()) {
				String hourKey = hes.getKey();
				double avgMemused = hes.getValue().stream().collect(Collectors.averagingDouble(st -> {
					double d = (double) st.getMemUsed() / (st.getMemFree() + st.getMemUsed());
					return d;
				}));
				avgMemused = avgMemused * 100;
				double avgLoad = hes.getValue().stream().collect(Collectors.averagingInt(st -> st.getAverageLoad()));
				ServerStateAvg ss = new ServerStateAvg();
				ss.setAvgLoad(avgLoad);
				ss.setAvgMemused(avgMemused);
				hourAvg.put(hourKey, ss);
			}
			byHoursAvg.put(dateKey, hourAvg);
		}
		return byHoursAvg;
	}

	private Map<String, Set<ServerState>> byDate(Collection<ServerState> ss) {
		return ss.stream().collect(Collectors.groupingBy(sst -> {
			return SQLTimeUtil.formatDate(sst.getCreatedAt());
		}, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));

	}

	public void createMem() {
		if (this.serverStates == null || this.serverStates.isEmpty()) {
			setMem("0");
		} else {
			ServerState ss = this.serverStates.get(0);
			setMem(StringUtil.formatSize(ss.getMemFree() + ss.getMemUsed()));
		}
	}

	public List<MysqlFlush> getMysqlFlushs() {
		return mysqlFlushs;
	}



	public List<MysqlDump> getMysqlDumps() {
		return mysqlDumps;
	}

	public List<BorgDownload> getBorgDownloads() {
		return borgDownloads;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public void setMysqlFlushs(List<MysqlFlush> mysqlFlushs) {
		this.mysqlFlushs = mysqlFlushs;
	}

	public List<ServerState> getServerStates() {
		return serverStates;
	}

	public void setServerStates(List<ServerState> serverStates) {
		this.serverStates = serverStates;
	}

	public List<StorageState> getStorageStates() {
		return storageStates;
	}

	public void setStorageStates(List<StorageState> storageStates) {
		this.storageStates = storageStates;
	}



	public void setMysqlDumps(List<MysqlDump> mysqlDumps) {
		this.mysqlDumps = mysqlDumps;
	}

	public void setBorgDownloads(List<BorgDownload> borgDownloads) {
		this.borgDownloads = borgDownloads;
	}

	public String getMem() {
		return mem;
	}

	public void setMem(String mem) {
		this.mem = mem;
	}

}
