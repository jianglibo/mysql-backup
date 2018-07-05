package com.go2wheel.mysqlbackup.tplcontext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.go2wheel.mysqlbackup.mail.ServerContext;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.value.ServerStateAvg;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.google.common.collect.Lists;

public class TestServerState {
	
	
	@Test
	public void t() throws IOException {
		Path pa = Paths.get("templates", "tplcontext.1.yml");
		String content = new String(Files.readAllBytes(pa), StandardCharsets.UTF_8);
		ServerGroupContext m = YamlInstance.INSTANCE.yaml.loadAs(content, ServerGroupContext.class);
		
		List<ServerContext> scs = m.getServers();
		assertThat(scs.size(), greaterThan(0));
		List<ServerState> ss = scs.get(0).getServerStates();
		assertThat(ss.size(), greaterThan(0));
		
		ServerContext sc = scs.get(0);
		Map<String, Map<String, ServerStateAvg>> byHours = sc.getServerStatebyHours();
		
		assertThat(byHours.size(), greaterThan(1));
	}
	
	@Test
	public void tStorageStateByDate() throws IOException {
		Path pa = Paths.get("templates", "tplcontext.1.yml");
		String content = new String(Files.readAllBytes(pa), StandardCharsets.UTF_8);
		ServerGroupContext m = YamlInstance.INSTANCE.yaml.loadAs(content, ServerGroupContext.class);
		
		List<ServerContext> scs = m.getServers();
		
		ServerContext sc = scs.get(0);
		Map<String, Map<String, StorageState>> byDate = sc.getStorageStateByDate();
		
		assertThat(byDate.size(), greaterThan(0));
	}
	
	private List<StorageState> createDemoStorageStates(int startYear, int startMonth, int startDay, int numberOfDays) {
		List<StorageState> lss = Lists.newArrayList();
		Calendar c = Calendar.getInstance();
		for(int i=0; i< numberOfDays;i++) {
			c.set(startYear, startMonth, startDay + i);
			StorageState ss = new StorageState();
			ss.setAvailable(100L);
			ss.setUsed(55L);
			ss.setRoot("/");
			ss.setCreatedAt(c.getTime());
			lss.add(ss);
		}
		return lss;
	}

	@Test
	public void tStorageStateByRoot() throws IOException {
		Path pa = Paths.get("templates", "tplcontext.1.yml");
		String content = new String(Files.readAllBytes(pa), StandardCharsets.UTF_8);
		ServerGroupContext m = YamlInstance.INSTANCE.yaml.loadAs(content, ServerGroupContext.class);
		List<ServerContext> scs = m.getServers();
		ServerContext sc = scs.get(0);

		sc = new ServerContext(Lists.newArrayList(), null, createDemoStorageStates(2018, 11, 3, 4), null, null);
		Map<String, Map<String, StorageState>> byRoot = sc.getStorageStateByRoot();
		Map<String, StorageState> date_percent = byRoot.entrySet().iterator().next().getValue();
		Set<String> dates = date_percent.keySet();
		assertThat(dates, contains("2018-12-03", "2018-12-04", "2018-12-05", "2018-12-06"));
	}
	
	private List<ServerState> createDemoServerStates(int dayoneTimes, int daytwoTimes) {
		Calendar c = Calendar.getInstance();
		List<ServerState> ls = Lists.newArrayList();
		ServerState ssa;
		
		for(int i = 0; i< dayoneTimes; i++) {
			ssa = new ServerState();
			c.set(2018, 11, 5, 10 + i, 30);
			ssa.setCreatedAt(c.getTime());
			ssa.setMemFree(60L);
			ssa.setMemUsed(35);
			ls.add(ssa);
		}
		
		for(int i = 0; i< daytwoTimes; i++) {
			ssa = new ServerState();
			c.set(2018, 11, 6, 10 + i, 30);
			ssa.setCreatedAt(c.getTime());
			ssa.setMemFree(60L);
			ssa.setMemUsed(35);
			ls.add(ssa);
		}
		return ls;
	}
	
	@Test
	public void tServerStateByHours() {
		ServerContext sc = new ServerContext(createDemoServerStates(3, 2), null, null, null, null);
		Map<String, Map<String, ServerStateAvg>> result = sc.getServerStatebyHours();
		assertThat(result.size(), equalTo(1));
		
		sc = new ServerContext(createDemoServerStates(3, 3), null, null, null, null);
		result = sc.getServerStatebyHours();
		assertThat(result.size(), equalTo(2));

	}


}
