package com.go2wheel.mysqlbackup.value;

import java.util.Map;
import java.util.stream.Collectors;

public class KkvWrapper {
	
	private Map<String, String> kvMap;
	
	public KkvWrapper(Map<String, String> kvMap) {
		this.setKvMap(kvMap);
	}

	public Map<String, String> getKvMap() {
		return kvMap;
	}

	public void setKvMap(Map<String, String> kvMap) {
		this.kvMap = kvMap;
	}

	public Map<String, String> getNestedMap(String partKey) {
		return kvMap.entrySet().stream().filter(en -> en.getKey().startsWith(partKey)).collect(Collectors.toMap(en -> {
			String k = en.getKey();
			k = k.substring(partKey.length());
			if (k.startsWith(".")) {
				k = k.substring(1);
			}
			return k;
		}, en -> en.getValue()));
	}

}
