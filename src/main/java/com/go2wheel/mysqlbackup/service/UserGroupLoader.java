package com.go2wheel.mysqlbackup.service;

import java.io.IOException;
import java.util.Collection;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MyAppSettings myAppSettings;

	@Autowired
	private ConfigFileLoader configFileLoader;


	public List<Subscribe> getAllSubscribes() {
		return null;
	}

	public void loadAll() throws JsonParseException, JsonMappingException, IOException {
		ConfigFileGroupFile cfg = objectMapper.readValue(myAppSettings.getGroupsFile().toFile(),
				ConfigFileGroupFile.class);

		for (ServerGrp gif : cfg.getGroups()) {
			String grpname = gif.getName();
			List<Server> cfs = gif.getHostnames().stream().map(hst -> configFileLoader.getByHostname(hst))
					.filter(Objects::nonNull).map(cf -> cf.asServer()).collect(Collectors.toList());
			ServerGrp grp = new ServerGrp();
			grp.setServers(cfs);
			groupCache.put(grpname, grp);
		}
		
		ConfigFileUserFile cfuf = objectMapper.readValue(myAppSettings.getUsersFile().toFile(),
				ConfigFileUserFile.class);
		
		for(UserAccount ua: cfuf.getUsers()) {
			userCache.put(ua.getName(), ua);
		}
		
		
		
//		userGroupCache = objectMapper.readValue(myAppSettings.getUsersFile().toFile(), UserGroupFile.class);
//
//		userGroupCache.getUsers().forEach(nu -> {
//			ServerGrp gi = groupCache.get(nu.getGroupname());
//			if (gi == null) {
//				logger.error("error usergroup file, groupname {} does'nt exists.", nu.getGroupname());
//			} else {
//				nu.setGroup(gi);
//			}
//		});
//
//		userGroupCache.getAdmins().forEach(nu -> {
//			ServerGrp gi = groupCache.get(nu.getGroupname());
//			if (gi == null) {
//				logger.error("error usergroup file, groupname {} does'nt exists.", nu.getGroupname());
//			} else {
//				nu.setGroup(gi);
//			}
//		});

	}

	public ServerGrp getGroupContent(String grpName) throws ExecutionException {
		return groupCache.get(grpName);
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

	public UserAccount getNotifyUser(String username) {
		return userGroupCache.users.stream().filter(nu -> nu.getName().equals(username)).findFirst().get();
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

	public Collection<Server> getServers(ServerGrp sg) {
		// TODO Auto-generated method stub
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
