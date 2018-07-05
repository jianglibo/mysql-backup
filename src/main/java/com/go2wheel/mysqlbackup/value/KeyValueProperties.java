package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.model.KeyValue;

public class KeyValueProperties extends Properties {
	
	private String prefix;
	
	private List<KeyValue> keyvalues;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public KeyValueProperties(List<KeyValue> kvs, String prefix) {
		super();
		this.prefix = prefix;
		this.keyvalues = kvs;
		parseKvs(kvs);
	}

	private void parseKvs(List<KeyValue> kvs) {
		int len = prefix.length() + 1;
		kvs.forEach(kv -> put(kv.getItemKey().substring(len), kv.getItemValue()));
	}
	
	public Optional<KeyValue> getKeyValue(String relativeKey) {
		String key = prefix + "." + relativeKey;
		return keyvalues.stream().filter(kv -> key.equals(kv.getItemKey())).findAny();
	}

	public List<String> getRelativeList(String relativePrefix) {
		final String kp = relativePrefix + "[";
		return keySet().stream().map(Objects::toString).filter(k -> k.startsWith(kp))
				.map(k -> getProperty(k))
				.collect(Collectors.toList());
	}

	public Map<String, String> getRelativeMap(String relativePrefix) {
		final int pl = relativePrefix.length() + 1;
		return keySet().stream().map(Objects::toString).filter(k -> k.startsWith(relativePrefix))
				.collect(Collectors.toMap(k -> k.substring(pl), this::getProperty));
	}
}
