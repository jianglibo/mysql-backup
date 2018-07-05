package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.KeyValue;

public class TestKeyValueProperties {

	@Test
	public void tGetProperty() {
		String group = "a.b";
		List<KeyValue> kvs = Arrays.asList(new KeyValue(group, "c"));
		KeyValueProperties kvp = new KeyValueProperties(kvs, "a");
		assertThat(kvp.getRelativeProperty("b"), equalTo("c"));
	}
	
	
	@Test
	public void tGetMap() {
		String prefix = "a.b.c";
		List<KeyValue> kvs = Arrays.asList(
				new KeyValue(prefix, "x", "1"),
				new KeyValue(prefix, "y", "2"),
				new KeyValue(prefix, "z", "3")
				);
		KeyValueProperties kvp = new KeyValueProperties(kvs, "a");
		assertThat(kvp.getRelativeProperty("b.c.x"), equalTo("1"));
		
		Map<String, String> subKvp = kvp.getMap("b");
		assertThat(subKvp.size(), equalTo(3));
		String mk =subKvp.keySet().iterator().next(); 
		assertTrue("should start with c..", mk.startsWith("c."));
	}
	
	@Test
	public void tGetList() {
		String prefix = "a.b";
		List<KeyValue> kvs = Arrays.asList(
				new KeyValue(prefix, "c[0]", "1"),
				new KeyValue(prefix, "c[1]", "2"),
				new KeyValue(prefix, "c[2]", "3"),
				new KeyValue(prefix, "d", "4")
				);
		KeyValueProperties kvp = new KeyValueProperties(kvs, "a.b");
		assertThat(kvp.getRelativeProperty("d"), equalTo("4"));
		
		List<String> list = kvp.getRelativeList("c");
		assertThat(list.size(), equalTo(3));

		assertThat(list, contains("1", "2", "3"));
	}

}
