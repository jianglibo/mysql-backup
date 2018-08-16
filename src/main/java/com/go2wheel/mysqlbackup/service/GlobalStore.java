package com.go2wheel.mysqlbackup.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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
	
	public static AtomicLong atomicLong = new AtomicLong(1L);

	private LoadingCache<String, Lock> lockCache;

	public Cache<String, CompletableFuture<AjaxResult>> groupListernerCache;

	private Map<String, Map<Long, SavedFuture>> sessionAndFutures = new HashMap<>();
	
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
	
	private void putToMap(String group, SavedFuture sf) {
		if (!sessionAndFutures.containsKey(group)) {
			sessionAndFutures.put(group, Maps.newHashMap());
		}
		sessionAndFutures.get(group).put(sf.getId(), sf);
	}

	
	/**
	 * 保存新的异步任务时，看看有没有在等待的long polling，如果有的话，用新任务的完成来满足long polling.
	 * 
	 * @param group
	 * @param key
	 * @param value
	 */
	public void saveFuture(String group, SavedFuture sf) {
		Lock lock = getLock(group);
		try {
			lock.lock();
			CompletableFuture<AjaxResult> lis = groupListernerCache.getIfPresent(group);
			if (lis != null) {
				if (lis.isDone()) {
					return;
				}
				sf.getCf().thenAccept(av -> {
					handleFutrueFacadeResult(av, lis); 
				}).exceptionally(throwable -> {
					ExceptionUtil.logThrowable(logger, throwable);
					lis.complete(AjaxErrorResult.exceptionResult(throwable));
					return null;
				});
			}
			putToMap(group, sf);
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}
	
	public void handleFutrueFacadeResult(AsyncTaskValue atv, CompletableFuture<AjaxResult> lis) {
		if (atv.getResult() instanceof FacadeResult) {
			FacadeResult<?> fr = (FacadeResult<?>) atv.getResult();
			if (!fr.isExpected()) {
				Throwable tw = fr.getException();
				if (tw != null) {
					if (tw instanceof ExceptionWrapper) {
						tw = ((ExceptionWrapper) tw).getException();
					}
					AjaxErrorResult aer = AjaxErrorResult.exceptionResult(tw);
					lis.complete(aer);
					futureFullfilled(atv.getId());
					return;
				}
			}
		} else if (atv.getThrowable() != null) {
			AjaxErrorResult aer = AjaxErrorResult.exceptionResult(atv.getThrowable());
			lis.complete(aer);
			futureFullfilled(atv.getId());
			return;
		}
		AjaxDataResult<?> arr = new AjaxDataResult<>();
		arr.addObject(atv);
		lis.complete(arr);
		futureFullfilled(atv.getId());
	}

	public SavedFuture getFuture(String group, Long id) {
		return sessionAndFutures.get(group).get(id);
	}
	
	public List<SavedFuture> getFutureGroupUnCompleted(String group) {
		return getFutureGroupAll(group).stream().filter(f -> !f.getCf().isDone()).collect(Collectors.toList());
	}
	
	public List<SavedFuture> getFutureGroupAll(String group) {
		Map<Long, SavedFuture> m = sessionAndFutures.get(group);
		if (m == null) {
			return new ArrayList<>();
		} else {
			return m.values().stream().collect(Collectors.toList());
		}
	}
	
	
	public SavedFuture removeFuture(SavedFuture sf) {
		return removeFuture(sf.getId());
	}
	
	public SavedFuture removeFuture(Long aid) {
		for (Map<Long, SavedFuture> map : sessionAndFutures.values()) {
			Optional<Entry<Long, SavedFuture>> ov = map.entrySet().stream().filter(es -> es.getKey().equals(aid)).findAny();
			if (ov.isPresent()) {
				return map.remove(ov.get().getKey());
			}
		}
		return null;
	}

	public StoreState getStoreState() {
		StoreState ss = new StoreState();
		ss.setGroupListernerCache(groupListernerCache.asMap().size());
		ss.setLockCache(lockCache.asMap().size());
		ss.setSessionAndFutures(sessionAndFutures.values().stream().mapToInt(m -> m.size()).sum());
		return ss;
	}
	
	public class StoreState {
		private int groupListernerCache;
		private int sessionAndFutures;
		private int lockCache;
		public int getGroupListernerCache() {
			return groupListernerCache;
		}
		public void setGroupListernerCache(int groupListernerCache) {
			this.groupListernerCache = groupListernerCache;
		}
		public int getSessionAndFutures() {
			return sessionAndFutures;
		}
		public void setSessionAndFutures(int sessionAndFutures) {
			this.sessionAndFutures = sessionAndFutures;
		}
		public int getLockCache() {
			return lockCache;
		}
		public void setLockCache(int lockCache) {
			this.lockCache = lockCache;
		}
	}
	
	/**
	 * We should keep completed futures for viewing in web page. At same time, when completing future we can obtain information for task from same source. 
	 * 
	 * @author jianglibo@gmail.com
	 *
	 */
	public static class SavedFuture {
		
		private Long id;
		private String description;
		private CompletableFuture<AsyncTaskValue> cf;
		private Instant startPoint;
		private Long millisecs;
		
		private SavedFuture() {}
		
		public static SavedFuture newSavedFuture(Long id, String description, CompletableFuture<AsyncTaskValue> cf) {
			SavedFuture savedFuture = new SavedFuture();
			savedFuture.setCf(cf);
			savedFuture.setDescription(description);
			savedFuture.setStartPoint(Instant.now());
			savedFuture.setId(id);
			return savedFuture;
		}
		
		public void stopCounting() {
			millisecs = Instant.now().toEpochMilli() - startPoint.toEpochMilli();
		}
		
		public String isExpected() {
			String result = "UNKNOWN";
			if (cf.isDone()) {
				Object o;
				try {
					AsyncTaskValue atv = cf.get();
					o = atv.getResult();
					if (o != null) {
						if (o instanceof FacadeResult) {
							FacadeResult<?> fo = (FacadeResult<?>) o;
							if (fo.isExpected()) {
								result = "DONE";
							} else {
								if (fo.getMessage() != null) {
									result = fo.getMessage();
								} else if (fo.getException() != null) {
									result = fo.getException().getMessage();
									if (result == null || result.isEmpty()) {
										result = fo.getException().getClass().getName();
									}
								}
							}
							result = (((FacadeResult<?>) o).isExpected() + "").toUpperCase();
						} else {
							result = o.toString();
						}
					} else {
						if (atv.getThrowable() != null) {
							String msg = atv.getThrowable().getMessage(); 
							if (msg == null || msg.isEmpty()) {
								msg = atv.getThrowable().getClass().getName();
							}
							return msg; 
						}
					}
				} catch (InterruptedException | ExecutionException e) {
				} 
			}
			return result;
		}
		
		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public CompletableFuture<AsyncTaskValue> getCf() {
			return cf;
		}

		public void setCf(CompletableFuture<AsyncTaskValue> cf) {
			this.cf = cf;
		}

		public Instant getStartPoint() {
			return startPoint;
		}

		public void setStartPoint(Instant startPoint) {
			this.startPoint = startPoint;
		}

		public String seconds() {
			long mms = 0;
			if (millisecs != null && millisecs > 0) {
				mms = millisecs;
			} else {
				mms = Instant.now().toEpochMilli() - startPoint.toEpochMilli();
			}
			return String.valueOf(mms / 1000);
		}

		public Long getMillisecs() {
			return millisecs;
		}

		public void setMillisecs(Long millisecs) {
			this.millisecs = millisecs;
		}
		
	}

	public void futureFullfilled(Long aid) {
		Optional<SavedFuture> sfo = sessionAndFutures.values().stream().flatMap(m -> m.values().stream()).filter(sf -> sf.getId().equals(aid)).findAny();
		if (sfo.isPresent()) {
			sfo.get().stopCounting();
		}
	}

	public void clearCompleted(String sid) {
		Map<Long, SavedFuture> lsmap = sessionAndFutures.get(sid);
		if (lsmap == null) return;
		Iterator<Entry<Long, SavedFuture>> it = lsmap.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Long, SavedFuture> es = it.next();
			if (es.getValue().getCf().isDone() && "TRUE".equals(es.getValue().isExpected())) {
				it.remove();
			}
		}
	}
	
}
