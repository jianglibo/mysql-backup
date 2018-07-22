package com.go2wheel.mysqlbackup;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.service.KeyValueDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

@Service
public class SettingsInDb {
	
	public static final String OSTYPE_PREFIX = "commons.ostype[";
	
	private static String[] predefines = new String[] {"linux_centos", "linux_centos_7", "win", "win_10", "win_2008", "win_2012"};
	

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private KeyValueDbService keyValueDbService;

	private LoadingCache<String, String> singleValueLc;

	private LoadingCache<String, List<String>> listValueLc;

	@PostConstruct
	private void post() {
		singleValueLc = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
			public String load(String key) {
				KeyValue kv = keyValueDbService.findOneByKey(key);
				if (kv == null) {
					kv = keyValueDbService.save(new KeyValue(key, ""));
				}
				return kv.getItemValue();
			}
		});
		
		listValueLc = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, List<String>>() {
			public List<String> load(String key) {
				List<KeyValue> kvs = keyValueDbService.findByKeyPrefix(key);
				return kvs.stream().map(kv -> kv.getItemValue()).collect(Collectors.toList());
			}
		});
		
		checkOsType();
		
	}
	
	private void checkOsType() {
		List<KeyValue> kvs = keyValueDbService.findByKeyPrefix(OSTYPE_PREFIX);
		if (kvs.isEmpty()) {
			for (int i = 0; i < predefines.length; i++) {
				KeyValue kv = new KeyValue(OSTYPE_PREFIX + i + "]", predefines[i]);
				try {
					keyValueDbService.save(kv);
				} catch (Exception e) {
					ExceptionUtil.logErrorException(logger, e);
				}
			}
		}
	}

	protected LoadingCache<String, String> getLc() {
		return singleValueLc;
	}

	@EventListener
	public void whenKeyValueChanged(ModelChangedEvent<KeyValue> keyValueChangedEvent) {
		KeyValue kv = keyValueChangedEvent.getAfter();
		singleValueLc.invalidate(kv.getItemKey());
	}

	public String getString(String key, String defaultValue) {
		try {
			String v = singleValueLc.get(key);
			if (v.isEmpty() && defaultValue != null && !defaultValue.isEmpty()) {
				KeyValue kv = keyValueDbService.findOneByKey(key);
				kv.setItemValue(defaultValue);
				keyValueDbService.save(kv);
				v = defaultValue;
			}
			return v;
		} catch (ExecutionException e) {
			ExceptionUtil.logErrorException(logger, e);
			return defaultValue;
		}
	}

	public String getString(String key) {
		return getString(key, "");

	}
	
	public List<String> getListString(String prefix) {
		try {
			return listValueLc.get(prefix);
		} catch (ExecutionException e) {
			ExceptionUtil.logErrorException(logger, e);
			return Lists.newArrayList();
		}
	}
	
	public int getInteger(String key, int defaultValue) {
		String v = getString(key, defaultValue + "");
		if (v == null || v.isEmpty()) {
			return 0;
		} else {
			return Integer.parseInt(v);
		}
	}
	
	public int getInteger(String key) {
		return getInteger(key, 0);
	}
}
