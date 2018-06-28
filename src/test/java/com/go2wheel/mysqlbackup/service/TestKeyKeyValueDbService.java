package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.quartz.SchedulerException;

import com.go2wheel.mysqlbackup.model.KeyKeyValue;
import com.go2wheel.mysqlbackup.value.KkvWrapper;

public class TestKeyKeyValueDbService extends ServiceTbase {

	@Test
	public void t() throws SchedulerException {
		keyKeyValueDbService.deleteAll();
		
		KeyKeyValue kkv = new KeyKeyValue("myapp", "ssh.sshIdrsa", "abc");
		
		keyKeyValueDbService.save(kkv);
		
		kkv = new KeyKeyValue("myapp", "ssh.knownHosts", "cde");
		
		keyKeyValueDbService.save(kkv);
		
		long count = keyKeyValueDbService.count();
		
		assertThat(count, equalTo(2L));
		
		KkvWrapper mp = keyKeyValueDbService.getGroup("myapp");
		
		assertThat(mp.getKvMap().size(), equalTo(2));
		
		Map<String, String> innerMap = mp.getNestedMap("ssh");
		
		assertThat(innerMap.size(), equalTo(2));
		assertThat(innerMap.get("sshIdrsa"), equalTo("abc"));
		assertThat(innerMap.get("knownHosts"), equalTo("cde"));

	}

}
