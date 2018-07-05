package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.model.KeyValue;

public class KeyValueProperties extends Properties {
	
	private String prefix;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public KeyValueProperties(List<KeyValue> kvs, String prefix) {
		super();
		this.prefix = prefix;
		parseKvs(kvs);
	}

	private void parseKvs(List<KeyValue> kvs) {
		kvs.forEach(kv -> put(kv.getItemKey(), kv.getItemValue()));
	}
	
	public String getRelativeProperty(String relativePrefix) {
		return super.getProperty(prefix + "." + relativePrefix);
	}

	public List<String> getRelativeList(String relativePrefix) {
		final String kp = this.prefix + "." + relativePrefix + "[";
		return keySet().stream().map(Objects::toString).filter(k -> k.startsWith(kp))
				.map(k -> getProperty(k))
				.collect(Collectors.toList());
	}

	public Map<String, String> getMap(String relativePrefix) {
		final String kp = this.prefix + "." + relativePrefix;
		final int pl = kp.length() + 1;
		return keySet().stream().map(Objects::toString).filter(k -> k.startsWith(kp))
				.collect(Collectors.toMap(k -> k.substring(pl), this::getProperty));
	}

	
}
