package com.go2wheel.mysqlbackup.util;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class EncodeConvertor {
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	private LoadingCache<String, String> singleValueLc;
	
	@PostConstruct
	private void post() {
		singleValueLc = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
			public String load(String origin) {
				String src = settingsInDb.getString("encode.src");
				String dst = settingsInDb.getString("encode.dst");
				
				if (src.isEmpty() || dst.isEmpty()) {
					return origin;
				} else {
					try {
						return new String(origin.getBytes(src), dst);
					} catch (UnsupportedEncodingException e) {
						return origin;
					}
				}
			}
		});
	}
	
	
	@EventListener
	public void whenKeyValueChanged(ModelChangedEvent<KeyValue> keyValueChangedEvent) {
		KeyValue kv = keyValueChangedEvent.getAfter();
		if (kv.getItemKey().equals("encode.src") || kv.getItemKey().equals("encode.dst")) {
			singleValueLc.invalidateAll();
		}
	}
	
	
	public String convert(String origin) {
		try {
			return singleValueLc.get(origin);
		} catch (ExecutionException e) {
			return origin;
		}
	}

}
