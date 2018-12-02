package com.go2wheel.mysqlbackup.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.util.TplUtil;
import com.go2wheel.mysqlbackup.value.ServerGrp;

public class ServerGroupContext {
	
	private UserAccount user;
	
	private ServerGrp serverGroup;
	
	private List<ServerContext> servers;
	
	private ServerContext myself;
	
	private TplUtil tplUtil;
	
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("user", user);
		map.put("servers", servers);
		map.put("serverGroup", serverGroup);
		map.put("tplUtil", new TplUtil());
		map.put("allHealthy", allHealthy());
		return map;
	}
	
	public boolean allHealthy() {
		boolean b = true;
		if (getServers() != null) {
			b = getServers().stream().allMatch(s -> ServerContext.NORMAL_MESSAGE_KEY.equals(s.healthy().getKey()));
		}
//		if (b && myself != null) {
//			return ServerContext.NORMAL_MESSAGE_KEY.equals(myself.healthy().getKey());
//		}
		return b;
	}
	
	public ServerGroupContext(List<ServerContext> servers, UserAccount user, ServerGrp serverGroup) {
		super();
		this.user = user;
		this.servers =servers;
		this.serverGroup = serverGroup;
		this.tplUtil = new TplUtil();
	}

	public ServerGrp getServerGroup() {
		return serverGroup;
	}

	public void setServerGroup(ServerGrp serverGroup) {
		this.serverGroup = serverGroup;
	}

	public List<ServerContext> getServers() {
		return servers;
	}

	public void setServers(List<ServerContext> servers) {
		this.servers = servers;
	}

	public UserAccount getUser() {
		return user;
	}


	public void setUser(UserAccount user) {
		this.user = user;
	}

	public ServerContext getMyself() {
		return myself;
	}

	public void setMyself(ServerContext myself) {
		this.myself = myself;
	}

	public TplUtil getTplUtil() {
		return tplUtil;
	}

	public void setTplUtil(TplUtil tplUtil) {
		this.tplUtil = tplUtil;
	}

}
