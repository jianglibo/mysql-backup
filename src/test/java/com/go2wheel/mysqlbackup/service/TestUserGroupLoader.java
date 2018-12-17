package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.MailBaseFort;
import com.go2wheel.mysqlbackup.value.Server;
import com.go2wheel.mysqlbackup.value.ServerGrp;
import com.go2wheel.mysqlbackup.value.Subscribe;
import com.go2wheel.mysqlbackup.value.UserAccount;

public class TestUserGroupLoader extends MailBaseFort {

	@Test
	public void tServers() throws Exception {
		Path[] pss = getPathes();
		userGroupLoader.clearAll();
		configFileLoader.clearCache();
		configFileLoader.loadAll(psappconfig, true);
		userGroupLoader.loadAll(pss[0], pss[1], pss[2], pss[3]);
		
		List<ServerGrp> grps = userGroupLoader.getAllGroups();
		assertThat(grps.size(), equalTo(1));
		ServerGrp grp = grps.get(0);
		assertThat(grp.getName(),equalTo("demogroup"));
		assertThat(grp.getHostnames().size(), equalTo(1));
		assertThat(grp.getServers().size(), equalTo(1));
		Server sv = grp.getServers().get(0);
		assertNotNull(sv.getName());
		
		List<UserAccount> users = userGroupLoader.getAllUsers();
		assertThat(users.size(), equalTo(1));
		
		UserAccount ua = users.get(0);
		assertThat(ua.getName(), equalTo("demouser"));
		assertThat(ua.getEmail(), equalTo("jianglibo@hotmail.com"));
		assertThat(ua.getMobile(), equalTo("13777272378"));
		assertThat(ua.getDescription(), equalTo(""));
		
		
		List<Subscribe> subscribes = userGroupLoader.getAllSubscribes();
		
		assertThat(subscribes.size(), equalTo(1));
		
		Subscribe sb = subscribes.get(0);
		assertThat(sb.getId(), equalTo("jlb"));
		assertThat(sb.getGroupname(), equalTo(grp.getName()));
		assertThat(sb.getTemplate(), equalTo("ctx.html"));
		assertThat(sb.getUsername(), equalTo(ua.getName()));
		assertNotNull(sb.getServerGroup());
		
		sv = sb.getServerGroup().getServers().get(0);
		
		assertThat(sv.getBorgArchiveResult(1).size(), equalTo(1));
		assertThat(sv.getBorgPruneResult(1).size(), equalTo(1));
		assertThat(sv.getDiskFreeResult(1).size(), equalTo(1));
		assertThat(sv.getMysqlDumpResult(1).size(), equalTo(1));
		assertThat(sv.getMysqlFlushResult(1).size(), equalTo(1));
	}

}
