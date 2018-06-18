package com.go2wheel.mysqlbackup.tplcontext;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;

import com.go2wheel.mysqlbackup.mail.ServerContext;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.value.ServerStateAvg;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

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
		
		assertThat(byHours.size(), greaterThan(2));
	}
	
	@Test
	public void tStorageState() throws IOException {
		Path pa = Paths.get("templates", "tplcontext.1.yml");
		String content = new String(Files.readAllBytes(pa), StandardCharsets.UTF_8);
		ServerGroupContext m = YamlInstance.INSTANCE.yaml.loadAs(content, ServerGroupContext.class);
		
		List<ServerContext> scs = m.getServers();
		
		ServerContext sc = scs.get(0);
		Map<String, Map<String, StorageState>> byDate = sc.getStorageStateByDate();
		
		assertThat(byDate.size(), greaterThan(0));
	}


}
