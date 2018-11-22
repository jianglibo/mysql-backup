package com.go2wheel.mysqlbackup.dbservice;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.StorageState;
import com.google.common.collect.Lists;

public class TestStorageStateComparator {
	
	
	@Test
	public void t() {
		StorageState st1 = new StorageState();
		st1.setRoot("a");
		
		StorageState st2 = new StorageState();
		st2.setRoot("b");
		st2.setAvailable(1);
		List<StorageState> ss = Lists.newArrayList(st1, st2);
		
		Collections.sort(ss, StorageState.AVAILABLE_DESC);
		assertThat(ss.get(0).getRoot(), equalTo("b"));
		
		st1.setAvailable(2);
		Collections.sort(ss, StorageState.AVAILABLE_DESC);
		assertThat(ss.get(0).getRoot(), equalTo("a"));
	}

}
