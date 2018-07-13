package com.go2wheel.mysqlbackup;

import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.service.KeyValueDbService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class SettingsInDb {

	@Autowired
	private KeyValueDbService keyValueDbService;

	private LoadingCache<String, String> lc;
	
	@PostConstruct
	private void post() {
		lc = CacheBuilder.newBuilder()
				.maximumSize(1000)
				.build(new CacheLoader<String, String>() {
					public String load(String key) {
						KeyValue kv = keyValueDbService.findOneByKey(key);
						if (kv == null) {
							kv = keyValueDbService.save(new KeyValue(key, ""));
						}
						return kv.getItemValue();
					}
				});
	}
	
	@EventListener
	public void whenKeyValueChanged(ModelChangedEvent<KeyValue> keyValueChangedEvent) {
		KeyValue kv = keyValueChangedEvent.getAfter();
		lc.invalidate(kv.getItemKey());
	}
	
	public String getString(String key) {
		try {
			return lc.get(key);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
}
