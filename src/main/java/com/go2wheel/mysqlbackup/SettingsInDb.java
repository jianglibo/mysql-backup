package com.go2wheel.mysqlbackup;

import java.util.concurrent.ExecutionException;

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

@Service
public class SettingsInDb {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private KeyValueDbService keyValueDbService;

	private LoadingCache<String, String> lc;

	@PostConstruct
	private void post() {
		lc = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
			public String load(String key) {
				KeyValue kv = keyValueDbService.findOneByKey(key);
				if (kv == null) {
					kv = keyValueDbService.save(new KeyValue(key, ""));
				}
				return kv.getItemValue();
			}
		});
	}
	
	protected LoadingCache<String, String> getLc() {
		return lc;
	}

	@EventListener
	public void whenKeyValueChanged(ModelChangedEvent<KeyValue> keyValueChangedEvent) {
		KeyValue kv = keyValueChangedEvent.getAfter();
		lc.invalidate(kv.getItemKey());
	}

	public String getString(String key, String defaultValue) {
		try {
			String v = lc.get(key);
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
