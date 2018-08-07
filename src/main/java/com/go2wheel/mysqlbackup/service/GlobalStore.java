package com.go2wheel.mysqlbackup.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.AjaxDataResult;
import com.go2wheel.mysqlbackup.value.AjaxErrorResult;
import com.go2wheel.mysqlbackup.value.AjaxResult;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

@Service
public class GlobalStore {

	private LoadingCache<String, Lock> lockCache;

	public Cache<String, CompletableFuture<AjaxResult>> groupListernerCache;

	private Map<String, Map<String, CompletableFuture<AsyncTaskValue>>> sessionAndFutures = new HashMap<>();

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	private void post() {
		lockCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(30)).maximumSize(1000)
				.build(new CacheLoader<String, Lock>() {
					public Lock load(String key) {
						return new ReentrantLock();
					}
				});
		groupListernerCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(30)).build();
	}

	public Lock getLock(String group) {
		try {
			return lockCache.get(group);
		} catch (ExecutionException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
		return null;
	}

	private void putToMap(String group, String key, CompletableFuture<AsyncTaskValue> value) {
		if (!sessionAndFutures.containsKey(group)) {
			sessionAndFutures.put(group, Maps.newHashMap());
		}
		sessionAndFutures.get(group).put(key, value);
	}

	/**
	 * 保存新的异步任务时，看看有没有在等待的long polling，如果有的话，用新任务的完成来满足long polling.
	 * 
	 * @param group
	 * @param key
	 * @param value
	 */
	public void saveAfuture(String group, String key, CompletableFuture<AsyncTaskValue> value) {
		Lock lock = getLock(group);
		try {
			lock.lock();
			CompletableFuture<AjaxResult> lis = groupListernerCache.getIfPresent(group);
			if (lis != null) {
				if (lis.isDone()) {
					return;
				}
				value.thenAccept(av -> {
					if (av.getResult() instanceof FacadeResult) {
						FacadeResult<?> fr = (FacadeResult<?>) av.getResult();
						if (!fr.isExpected()) {
							Throwable tw = fr.getException();
							if (tw != null) {
								if (tw instanceof ExceptionWrapper) {
									tw = ((ExceptionWrapper) tw).getException();
								}
								AjaxErrorResult aer = AjaxErrorResult.exceptionResult(tw);
								removeFuture(value);
								lis.complete(aer);
								return;
							}
							AjaxDataResult<?> arr = new AjaxDataResult<>();
							arr.addObject(av);
							removeFuture(value);
							lis.complete(arr);
							return;
						}
					}
					AjaxDataResult<?> fr = new AjaxDataResult<>();
					fr.addObject(av);
					removeFuture(value);
					lis.complete(fr);
				}).exceptionally(throwable -> {
					ExceptionUtil.logThrowable(logger, throwable);
					removeFuture(value);
					lis.complete(AjaxErrorResult.exceptionResult(throwable));
					return null;
				});
			}
			putToMap(group, key, value);
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}

	public CompletableFuture<AsyncTaskValue> getFuture(String group, String key) {
		return sessionAndFutures.get(group).get(key);
	}

	public CompletableFuture<AsyncTaskValue> removeFuture(String group, String key) {
		return sessionAndFutures.get(group).remove(key);
	}

	public List<CompletableFuture<AsyncTaskValue>> getGroupObjects(String group) {
		return new ArrayList<>(sessionAndFutures.get(group).values());

	}

	public void removeFuture(CompletableFuture<AsyncTaskValue> it) {
		for (Map<String, CompletableFuture<AsyncTaskValue>> map : sessionAndFutures.values()) {
			Optional<Entry<String, CompletableFuture<AsyncTaskValue>>> ov = map.entrySet().stream().filter(es -> es.getValue() == it).findAny();
			if (ov.isPresent()) {
				map.remove(ov.get().getKey());
				break;
			}
		}
	}

}
