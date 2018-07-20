package com.go2wheel.mysqlbackup.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class GlobalStore {
	
	private Map<String, Object> obStore = new HashMap<>();
	
	private String combine(String group, String key) {
		return group + "." + key;
	}
	
	public void saveDeferred(String group, String key, Object value) {
		obStore.put(combine(group, key), value);
	}
	
	public Object getDeferred(String group, String key) {
		String k = combine(group, key);
		if (obStore.containsKey(k)) {
			Object o = obStore.get(k);
			obStore.remove(k);
			return o;
		}
		return null;
	}

}
