package com.go2wheel.mysqlbackup.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.AjaxDataResult;
import com.go2wheel.mysqlbackup.value.AjaxErrorResult;
import com.go2wheel.mysqlbackup.value.AjaxResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class GlobalStore {

	private LoadingCache<String, Lock> lockCache;
	
	public Cache<String, CompletableFuture<AjaxResult>> groupListernerCache;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	private void post() {
		lockCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(30)).maximumSize(1000).build(new CacheLoader<String, Lock>() {
			public Lock load(String key) {
				return new ReentrantLock();
			}
		});
		groupListernerCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(30)).build();
	}

	private Map<String, Gobject> obStore = new HashMap<>();
	

	public Lock getLock(String group) {
		try {
			return lockCache.get(group);
		} catch (ExecutionException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
		return null;
	}

	private String combine(String group, String key) {
		return group + "." + key;
	}

	/**
	 * 保存新的异步任务时，看看有没有在等待的long polling，如果有的话，用新任务的完成来满足long polling.
	 * 
	 * @param group
	 * @param key
	 * @param value
	 */
	public void saveObject(String group, String key, Gobject value) {
		Lock lock = getLock(group);
		try {
			lock.lock();
			CompletableFuture<AjaxResult> lis = groupListernerCache.getIfPresent(group);
			if (lis != null) {
				if (lis.isDone()) {
					return;
				}
				@SuppressWarnings("unchecked")
				CompletableFuture<Object> cf1 = (CompletableFuture<Object>) value.getObject();
				cf1.thenAccept(r -> {
					AjaxDataResult<?> fr = new AjaxDataResult<>();
					fr.addObject(r);
					removeObject(cf1);
					lis.complete(fr);
				}).exceptionally(throwable -> {
					ExceptionUtil.logThrowable(logger, throwable);
					removeObject(cf1);
					lis.complete(AjaxErrorResult.exceptionResult(throwable));
					return null;
				});
			}
			obStore.put(combine(group, key), value);
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}

	public Gobject getObject(String group, String key) {
		String k = combine(group, key);
		return obStore.get(k);
	}

	public Gobject removeObject(String group, String key) {
		return obStore.remove(combine(group, key));
	}

	public List<Gobject> getGroupObjects(String groupName) {
		return obStore.entrySet().stream().filter(es -> es.getKey().startsWith(groupName + "."))
				.map(es -> es.getValue()).collect(Collectors.toList());

	}

	public void removeObject(Object it) {
		Optional<Entry<String, Gobject>> s = obStore.entrySet().stream().filter(es -> es.getValue().getObject() == it)
				.findAny();
		if (s.isPresent()) {
			obStore.remove(s.get().getKey());
		}
	}

	public static class Gobject {

		private String name;
		private Object object;

		public static Gobject newGobject(String name, Object object) {
			return new Gobject(name, object);
		}

		public Gobject(String name, Object object) {
			super();
			this.name = name;
			this.object = object;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Object getObject() {
			return object;
		}

		public void setObject(Object object) {
			this.object = object;
		}

		@SuppressWarnings("unchecked")
		public <T> T as(Class<T> clazz) {
			return (T) object;
		}
	}

}
