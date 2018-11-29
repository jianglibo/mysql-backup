package com.go2wheel.mysqlbackup.mail;

import com.go2wheel.mysqlbackup.value.MsgKeyAndFills2;
import com.go2wheel.mysqlbackup.value.PsBorgAchiveResult;
import com.go2wheel.mysqlbackup.value.PsDiskMemFreeResult;
import com.go2wheel.mysqlbackup.value.PsMysqldumpResult;
import com.go2wheel.mysqlbackup.value.PsMysqlflushResult;
import com.go2wheel.mysqlbackup.value.Server;

public class ServerContext {
	
	public static final String NORMAL_MESSAGE_KEY = "server.state.normal";
	public static final String DISK_FULL_MESSAGE_KEY = "server.state.diskfull";
	public static final String MEMROY_FULL_MESSAGE_KEY = "server.state.memoryfull";
	public static final String HEAVY_LOAD_MESSAGE_KEY = "server.state.heavyload";
	
	private PsDiskMemFreeResult memfrees;
	private PsDiskMemFreeResult diskfrees;
	
	private PsBorgAchiveResult borgArchiveResult;
	
	private PsBorgAchiveResult borgPruneResult;
	
	private PsMysqldumpResult mysqlDumpResult;
	
	private PsMysqlflushResult mysqlFlushResult;
	
	private Server server;

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
	public ServerContext(PsDiskMemFreeResult diskfrees,PsDiskMemFreeResult memfrees, 
			PsBorgAchiveResult borgArchiveResult, PsBorgAchiveResult borgPruneResult,PsMysqldumpResult mysqlDumpResult, PsMysqlflushResult mysqlFlushResult) {
		super();
		this.borgArchiveResult = borgArchiveResult;
		this.borgPruneResult = borgPruneResult;
		this.mysqlDumpResult = mysqlDumpResult;
		this.mysqlFlushResult = mysqlFlushResult;
		this.memfrees = memfrees;
		this.diskfrees = diskfrees;
	}
	
	public MsgKeyAndFills2 healthy() {
//		if (getMemfrees() != null) {
//			double avd = getMemfrees().getResult().get(0).getPercent();
//			if (avd > server.getMemoryValve()) {
//				return new MsgKeyAndFills2(MEMROY_FULL_MESSAGE_KEY, avd, server.getMemoryValve());
//			}
//		}
//		
//		if (getStorageStates() != null) {
//			OptionalDouble od = getStorageStates().stream().mapToDouble(one -> one.getUsedRatio()).max();
//			double avd = od.isPresent() ? od.getAsDouble() : 0d;
//			if (avd > server.getDiskValve()) {
//				return new MsgKeyAndFills2(DISK_FULL_MESSAGE_KEY, avd, server.getDiskValve());
//			}
//		}
		return new MsgKeyAndFills2(NORMAL_MESSAGE_KEY);
	}

	/**
	 * The date as key. root and percent as a pair.
	 * @return
	 */
//	public Map<String, Map<String, StorageState>> getStorageStateByDate() {
//		Map<String, Set<StorageState>> stss = getStorageStates().stream().collect(Collectors.groupingBy(sst -> {
//			return SQLTimeUtil.formatDate(sst.getCreatedAt());
//		}, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));
//
//		Map<String, Map<String, StorageState>> byDateAndRoot = new TreeMap<>();
//
//		for (Map.Entry<String, Set<StorageState>> es : stss.entrySet()) {
//			String dateKey = es.getKey();
//			
//			Set<StorageState> dailyData = es.getValue(); // date as the key.
//			
//			// root as key, maybe has multiple values, if we run program twice a day.
//			Map<String, Set<StorageState>> oneByRoot = dailyData.stream().collect(Collectors.groupingBy(
//					StorageState::getRoot, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));
//			
//			// get one record a day. If there was more than one record discard it.
//			Map<String, StorageState> onlyOneRecord = new HashMap<>();
//			
//			for(Map.Entry<String, Set<StorageState>> oneRootItem : oneByRoot.entrySet()) {
//				onlyOneRecord.put(oneRootItem.getKey(), oneRootItem.getValue().iterator().next());
//			}
//			
//			byDateAndRoot.put(dateKey, onlyOneRecord);
//		}
//		return byDateAndRoot;
//	}
//	
	/**
	 * mount root as key. date and percent pair as value. 
	 * @return
	 */
//	public Map<String, Map<String, StorageState>> getStorageStateByRoot() {
//		Map<String, Set<StorageState>> stss = getStorageStates().stream().collect(Collectors.groupingBy(sst -> {
//			return sst.getRoot();
//		}, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));
//
//		Map<String, Map<String, StorageState>> byRootAndDate = new TreeMap<>();
//
//		for (Map.Entry<String, Set<StorageState>> es : stss.entrySet()) {
//			String rootKey = es.getKey();
//			Set<StorageState> rootData = es.getValue(); // all storageStates about one root, span multiple days.
//			
//			// date as the key, maybe has multiple values, if we run program twice a day.
//			Map<String, Set<StorageState>> oneByDate = rootData.stream().collect(Collectors.groupingBy(sst ->
//					SQLTimeUtil.formatDate(sst.getCreatedAt()), TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));
//			
//			// get one record for one root in one day. If there was more than one record discard it.
//			Map<String, StorageState> onlyOneRecord = new TreeMap<>();
//			
//			for(Map.Entry<String, Set<StorageState>> oneDateItem : oneByDate.entrySet()) {
//				onlyOneRecord.put(oneDateItem.getKey() /*it's date.*/, oneDateItem.getValue().iterator().next());
//			}
//			
//			byRootAndDate.put(rootKey, onlyOneRecord);
//		}
//		return byRootAndDate;
//	}

//	public Map<String, Map<String, ServerStateAvg>> getServerStatebyHours() {
//		Map<String, Set<ServerState>> byDate = byDate(getServerStates());
//
//		Map<String, Map<String, Set<ServerState>>> byHours = new TreeMap<>();
//		DateFormat df = new SimpleDateFormat("HH");
//
//		for (Map.Entry<String, Set<ServerState>> es : byDate.entrySet()) {
//			if (es.getValue().size() < 4) continue;
//			Map<String, Set<ServerState>> oneByDate = es.getValue().stream().collect(Collectors.groupingBy(sst -> {
//				String k = df.format(sst.getCreatedAt());
//				return k;
//			}, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));
//			byHours.put(es.getKey(), oneByDate);
//		}
//		Map<String, Map<String, ServerStateAvg>> byHoursAvg = new TreeMap<>();
//		
//
//		for (Map.Entry<String, Map<String, Set<ServerState>>> es : byHours.entrySet()) {
//			String dateKey = es.getKey(); // key is date. 1982-01-12
//			Map<String, Set<ServerState>> hourValue = es.getValue();
//
//			Map<String, ServerStateAvg> hourAvg = new TreeMap<>();
//
//			for (Map.Entry<String, Set<ServerState>> hes : hourValue.entrySet()) {
//				String hourKey = hes.getKey();
//				double avgMemused = hes.getValue().stream().collect(Collectors.averagingDouble(st -> {
//					double d = (double) st.getMemUsed() / (st.getMemFree() + st.getMemUsed());
//					return d;
//				}));
//				avgMemused = avgMemused * 100;
//				double avgLoad = hes.getValue().stream().collect(Collectors.averagingInt(st -> st.getAverageLoad()));
//				ServerStateAvg ss = new ServerStateAvg();
//				ss.setAvgLoad(avgLoad);
//				ss.setAvgMemused(avgMemused);
//				hourAvg.put(hourKey, ss);
//			}
//			byHoursAvg.put(dateKey, hourAvg);
//		}
//		return byHoursAvg;
//	}

//	private Map<String, Set<ServerState>> byDate(Collection<ServerState> ss) {
//		return ss.stream().collect(Collectors.groupingBy(sst -> {
//			return SQLTimeUtil.formatDate(sst.getCreatedAt());
//		}, TreeMap::new, Collectors.mapping(sst -> sst, Collectors.toSet())));
//
//	}
	

	public Server getServer() {
		return server;
	}

	public PsDiskMemFreeResult getMemfrees() {
		return memfrees;
	}

	public void setMemfrees(PsDiskMemFreeResult memfrees) {
		this.memfrees = memfrees;
	}

	public PsDiskMemFreeResult getDiskfrees() {
		return diskfrees;
	}

	public void setDiskfrees(PsDiskMemFreeResult diskfrees) {
		this.diskfrees = diskfrees;
	}

	public PsBorgAchiveResult getBorgArchiveResult() {
		return borgArchiveResult;
	}

	public void setBorgArchiveResult(PsBorgAchiveResult borgArchiveResult) {
		this.borgArchiveResult = borgArchiveResult;
	}

	public PsBorgAchiveResult getBorgPruneResult() {
		return borgPruneResult;
	}

	public void setBorgPruneResult(PsBorgAchiveResult borgPruneResult) {
		this.borgPruneResult = borgPruneResult;
	}

	public PsMysqldumpResult getMysqlDumpResult() {
		return mysqlDumpResult;
	}

	public void setMysqlDumpResult(PsMysqldumpResult mysqlDumpResult) {
		this.mysqlDumpResult = mysqlDumpResult;
	}

	public PsMysqlflushResult getMysqlFlushResult() {
		return mysqlFlushResult;
	}

	public void setMysqlFlushResult(PsMysqlflushResult mysqlFlushResult) {
		this.mysqlFlushResult = mysqlFlushResult;
	}

	public void setServer(Server server) {
		this.server = server;
	}

}
