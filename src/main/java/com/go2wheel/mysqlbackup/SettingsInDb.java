package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.KeyValueDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

@Service
public class SettingsInDb {
	
	public static final int POST_FIX_LENGTH_7 = 7;
	
	public static final String OSTYPE_PREFIX = "commons.ostype[";
	
	private static String[] predefines = new String[] {"linux_centos", "linux_centos_7", "win", "win_10", "win_2008", "win_2012"};
	
	public static final String DOWNLOAD_FOLDER_KEY = "installer.download";
	public static final String APP_DATA_DIR_KEY = "app.data-dir";
	

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private KeyValueDbService keyValueDbService;

	private LoadingCache<String, String> singleValueLc;

	private LoadingCache<String, List<String>> listValueLc;
	
	private Path downloadPath;
	
	private Path dataDir;
	
	@PostConstruct
	private void post() throws IOException {
		singleValueLc = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
			public String load(String key) {
				KeyValue kv = keyValueDbService.findOneByKey(key);
				if (kv == null) {
					kv = keyValueDbService.save(new KeyValue(key, ""));
				}
				return kv.getItemValue();
			}
		});
		
		listValueLc = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, List<String>>() {
			public List<String> load(String key) {
				List<KeyValue> kvs = keyValueDbService.findByKeyPrefix(key);
				return kvs.stream().map(kv -> kv.getItemValue()).collect(Collectors.toList());
			}
		});
		
		checkOsType();
		makeDirectories();
	}
	
	private void makeDirectories() throws IOException {
		downloadPath = Paths.get(getString(SettingsInDb.DOWNLOAD_FOLDER_KEY, "notingit/download"));
		if (!Files.exists(downloadPath)) {
			Files.createDirectories(downloadPath);
		}
		
		dataDir = Paths.get(getString(SettingsInDb.APP_DATA_DIR_KEY, "servers"));
		if (!Files.exists(dataDir)) {
			Files.createDirectories(dataDir);
		}

	}

	private void checkOsType() {
		List<KeyValue> kvs = keyValueDbService.findByKeyPrefix(OSTYPE_PREFIX);
		if (kvs.isEmpty()) {
			for (int i = 0; i < predefines.length; i++) {
				KeyValue kv = new KeyValue(OSTYPE_PREFIX + i + "]", predefines[i]);
				try {
					keyValueDbService.save(kv);
				} catch (Exception e) {
					ExceptionUtil.logErrorException(logger, e);
				}
			}
		}
	}

	protected LoadingCache<String, String> getLc() {
		return singleValueLc;
	}

	@EventListener
	public void whenKeyValueChanged(ModelChangedEvent<KeyValue> keyValueChangedEvent) {
		KeyValue kv = keyValueChangedEvent.getAfter();
		singleValueLc.invalidate(kv.getItemKey());
	}

	public String getString(String key, String defaultValue) {
		try {
			String v = singleValueLc.get(key);
			if (v.isEmpty() && defaultValue != null && !defaultValue.isEmpty()) {
				KeyValue kv = keyValueDbService.findOneByKey(key);
				kv.setItemValue(defaultValue);
				keyValueDbService.save(kv);
				v = defaultValue;
			}
			return v;
		} catch (ExecutionException e) {
			ExceptionUtil.logErrorException(logger, e);
			return defaultValue;
		}
	}

	public String getString(String key) {
		return getString(key, "");

	}
	
	public List<String> getListString(String prefix) {
		try {
			return listValueLc.get(prefix);
		} catch (ExecutionException e) {
			ExceptionUtil.logErrorException(logger, e);
			return Lists.newArrayList();
		}
	}
	
	public int getInteger(String key, int defaultValue) {
		String v = getString(key, defaultValue + "");
		if (v == null || v.isEmpty()) {
			return 0;
		} else {
			return Integer.parseInt(v);
		}
	}
	
	public int getInteger(String key) {
		return getInteger(key, 0);
	}
	
	public Path getDownloadPath() {
		return downloadPath;
	}

	public Path getDataDir() {
		return dataDir;
	}
	
	private Path getDirInHost(Server server, String relative) {
		Path hostDir = getDataDir().resolve(server.getHost());
		return hostDir.resolve(relative);
	}
	
	/**
	 * 
	 * @param server
	 * @return The base repo name. For example returning /repo , but not /repo.0 /repo.1 etc.
	 * @throws IOException 
	 */
	public Path getRepoDirBase(Server server) throws IOException {
		return getDirInHost(server, "repos/repo");
	}
	
	public Path getRepoTmp(Server server) throws IOException {
		return createIfNotExists(getDirInHost(server, "tmp"));
	}
	
	public Path getReposDir(Server server) throws IOException {
		Path p = getDirInHost(server, "repos");
		if (!Files.exists(p)) {
			Files.createDirectories(p);
		}
		return p;
	}
	
	public Path getCurrentRepoDir(Server server) throws IOException {
		Path path = getReposDir(server).resolve("repo"); 
		path = PathUtil.getMaxVersionByBaseName(path, POST_FIX_LENGTH_7);
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
		return path;
	}
	
	public Path getNextRepoDir(Server server) throws IOException {
		Path path = getReposDir(server).resolve("repo"); 
		Files.list(path.getParent()).forEach(p -> {
			try {
				if (Files.list(p).toArray().length == 0) {
					Files.delete(p);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return PathUtil.getNextAvailableByBaseName(path, POST_FIX_LENGTH_7);
	}
	
	private Path createIfNotExists(Path path) throws IOException {
		if (!Files.exists(path)) {
			try {
				return Files.createDirectories(path);
			} catch (Exception e) {
				e.printStackTrace();
				return path;
			}
		} else {
			return path;			
		}
	}
	
	/**
	 *  
	 * @param server
	 * @return max version folder.
	 * @throws IOException
	 */
	public Path getCurrentDumpDir(Server server) throws IOException {
		Path path = getDumpsDir(server).resolve("dump"); 
		path =  PathUtil.getMaxVersionByBaseName(path, POST_FIX_LENGTH_7);
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
		return path;
	}
	
	public Path getDumpDirBase(Server server) {
		return getDirInHost(server, "dumps/dump");
	}
	
	public Path getDumpsDir(Server server) throws IOException {
		Path p = getDirInHost(server, "dumps");
		if (!Files.exists(p)) {
			Files.createDirectories(p);
		}
		return p;
	}
	
	/**
	 * @param server
	 * @return Always return a new empty folder.
	 * @throws IOException
	 */
	public Path getNextDumpDir(Server server) throws IOException {
		Path path = getDumpsDir(server).resolve("dump"); 
		Files.list(path.getParent()).forEach(p -> {
			try {
				if (Files.list(p).toArray().length == 0) {
					Files.delete(p);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return PathUtil.getNextAvailableByBaseName(path, POST_FIX_LENGTH_7);
	}
	
	public Path getLocalMysqlDir(Server server) throws IOException {
		return createIfNotExists(getDirInHost(server, "mysql"));
	}
}
