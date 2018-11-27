package com.go2wheel.mysqlbackup.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

@Service
public class UserGroupLoader {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, ServerGrp> groupCache = new HashMap<>();
	private Map<String, UserAccount> userCache = new HashMap<>();
	
	private Map<String, Subscribe> subscribesCache = new HashMap<>();
	
	private List<UserAccount> adminUserCache = new ArrayList<>();

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MyAppSettings myAppSettings;

	@Autowired
	private ConfigFileLoader configFileLoader;


	public List<Subscribe> getAllSubscribes() {
		return null;
	}
	
	public void loadAll() throws Exception {
		loadAll(myAppSettings.getGroupsFile(), myAppSettings.getUsersFile(), myAppSettings.getSubscribeFile(), myAppSettings.getAdminFile());
	}

	public void loadAll(Path groupsFilePath, Path usersFilePath, Path subscribesFilePath, Path adminFilePath) throws Exception {
		ConfigFileGroupFile cfg = objectMapper.readValue(groupsFilePath.toFile(),
				ConfigFileGroupFile.class);

		for (ServerGrp gif : cfg.getGroups()) {
			String grpname = gif.getName();
			List<Server> cfs = gif.getHostnames().stream().map(hst -> configFileLoader.getByHostname(hst))
					.filter(Objects::nonNull).map(cf -> cf.asServer()).collect(Collectors.toList());
			ServerGrp grp = new ServerGrp();
			grp.setServers(cfs);
			groupCache.put(grpname, grp);
		}
		
		ConfigFileUserFile cfuf = objectMapper.readValue(usersFilePath.toFile(),
				ConfigFileUserFile.class);
		
		for(UserAccount ua: cfuf.getUsers()) {
			userCache.put(ua.getName(), ua);
		}
		
		ConfigFileSubscribeFile subscribeFile = objectMapper.readValue(subscribesFilePath.toFile(),
				ConfigFileSubscribeFile.class);
		
		for(Subscribe sb: subscribeFile.getSubscribes()) {
			subscribesCache.put(sb.getId(), sb);
		}
		
		ConfigFileSubscribeAdminFile subscribeAdminFile = objectMapper.readValue(adminFilePath.toFile(),
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

	public Object getAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAllGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	public Subscribe getSubscribeById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Server> getServersInGroup(ServerGrp sg) {
		return null;
	}

	public List<ServerGrp> findLikeEname(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<UserAccount> findUserAccountLikeName(String input) {
		// TODO Auto-generated method stub
		return null;
	}
}
