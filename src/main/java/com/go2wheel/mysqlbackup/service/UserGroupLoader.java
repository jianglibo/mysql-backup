package com.go2wheel.mysqlbackup.service;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.value.ConfigFile;

@Service
public class UserGroupLoader {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, ServerGrp> groupCache = new HashMap<>();
	private Map<String, UserAccount> userCache = new HashMap<>();
	
	private Map<String, Server> serverCache = new HashMap<>();
	
	private Map<String, Subscribe> subscribesCache = new HashMap<>();
	
	private List<UserAccount> adminUserCache = new ArrayList<>();

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MyAppSettings myAppSettings;

	@Autowired
	private ConfigFileLoader configFileLoader;


	public List<Subscribe> getAllSubscribes() {
		return new ArrayList<>(subscribesCache.values());
	}
	
	protected void clearAll() {
		groupCache.clear();
		userCache.clear();
		subscribesCache.clear();
		adminUserCache.clear();
	}
	
	public void loadAll() throws Exception {
		loadAll(myAppSettings.getGroupsFile(), myAppSettings.getUsersFile(), myAppSettings.getSubscribeFile(), myAppSettings.getAdminFile());
	}
	
	private Reader readContentUtf8(Path file) throws IOException {
		return Files.newBufferedReader(file, StandardCharsets.UTF_8);
	}
	
	public void loadAll(Path groupsFilePath, Path usersFilePath, Path subscribesFilePath, Path adminFilePath) throws Exception {
		
		ConfigFileGroupFile cfg = objectMapper.readValue(readContentUtf8(groupsFilePath),
				ConfigFileGroupFile.class);

		for (ServerGrp grp : cfg.getGroups()) {
			String grpname = grp.getName();
			List<Server> servers = new ArrayList<>();
			
			for(String hostname: grp.getHostnames()) {
				Server sv;
				ConfigFile cf = configFileLoader.getByHostname(hostname);
				if (cf == null) {
					String message = "hostname in server group hostnames does't contain " + hostname;
					throw new Exception(message);
				}
				if (serverCache.containsKey(hostname)) {
					sv = serverCache.get(hostname);
				} else {
					sv = new Server();
					sv.setName(cf.getServerName());
					sv.setHost(cf.getHostName());
					serverCache.put(hostname, sv);
				}
				sv.getConfigFiles().add(cf);
				servers.add(sv);
			}
			grp.setServers(servers);
			groupCache.put(grpname, grp);
		}
		
		ConfigFileUserFile cfuf = objectMapper.readValue(readContentUtf8(usersFilePath),
				ConfigFileUserFile.class);
		
		for(UserAccount ua: cfuf.getUsers()) {
			userCache.put(ua.getName(), ua);
		}
		
		ConfigFileSubscribeFile subscribeFile = objectMapper.readValue(readContentUtf8(subscribesFilePath),
				ConfigFileSubscribeFile.class);
		
		for(Subscribe sb: subscribeFile.getSubscribes()) {
			String groupname = sb.getGroupname();
			ServerGrp grp = groupCache.get(groupname);
			if (grp == null) {
				String message = "subscribe file  groupname does't exists: " + groupname;
				throw new Exception(message);
			}
			sb.setServerGroup(grp);
			subscribesCache.put(sb.getId(), sb);
		}
		
		ConfigFileSubscribeAdminFile subscribeAdminFile = objectMapper.readValue(readContentUtf8(adminFilePath),
				ConfigFileSubscribeAdminFile.class);
		
		for(String username: subscribeAdminFile.getAdmins()) {
			UserAccount sb = userCache.get(username);
			if (sb == null) {
				String message = "admin id " + username + " is'nt a valid userid.";
				logger.error(message);
				throw new Exception(message);
			}
			adminUserCache.add(sb);
		}
	}

	public ServerGrp getGroupByName(String grpName) throws ExecutionException {
		return groupCache.get(grpName);
	}
	
	public static class ConfigFileSubscribeAdminFile {
		private List<String> admins;

		public List<String> getAdmins() {
			return admins;
		}

		public void setAdmins(List<String> admins) {
			this.admins = admins;
		}
	}
	
	public static class ConfigFileSubscribeFile {
		private List<Subscribe> subscribes;

		public List<Subscribe> getSubscribes() {
			return subscribes;
		}

		public void setSubscribes(List<Subscribe> subscribes) {
			this.subscribes = subscribes;
		}
	}

	public static class ConfigFileGroupFile {
		private List<ServerGrp> groups;

		public List<ServerGrp> getGroups() {
			return groups;
		}

		public void setGroups(List<ServerGrp> groups) {
			this.groups = groups;
		}
	}

	public static class ConfigFileUserFile {

		private List<UserAccount> users;

		public List<UserAccount> getUsers() {
			return users;
		}

		public void setUsers(List<UserAccount> users) {
			this.users = users;
		}
	}

	public UserAccount getUserByName(String username) {
		return userCache.get(username);
	}

	public List<UserAccount> getAllUsers() {
		return new ArrayList<>(userCache.values());
	}

	public List<ServerGrp> getAllGroups() {
		return new ArrayList<>(groupCache.values());
	}

	public Subscribe getSubscribeById(String id) {
		return null;
	}

	public List<ServerGrp> findLikeEname(String input) {
		return null;
	}

	public List<UserAccount> findUserAccountLikeName(String input) {
		return null;
	}
}
