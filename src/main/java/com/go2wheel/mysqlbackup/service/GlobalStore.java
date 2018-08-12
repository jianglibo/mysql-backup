package com.go2wheel.mysqlbackup.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
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
	
//	private Map<CompletableFuture<AsyncTaskValue>, TimeElapsed> timeCostMap = Maps.newHashMap();

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


//	private void putToMap(String group, String key, CompletableFuture<AsyncTaskValue> value) {
//		if (!sessionAndFutures.containsKey(group)) {
//			sessionAndFutures.put(group, Maps.newHashMap());
//		}
//		sessionAndFutures.get(group).put(key, value);
//	}
	
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
		}
		AjaxDataResult<?> arr = new AjaxDataResult<>();
		arr.addObject(atv);
		lis.complete(arr);
		futureFullfilled(atv.getId());
	}

	/**
	 * 保存新的异步任务时，看看有没有在等待的long polling，如果有的话，用新任务的完成来满足long polling.
	 * 
	 * @param group
	 * @param key
	 * @param value
	 */
//	public void saveAfuture(String group, String key, CompletableFuture<AsyncTaskValue> value) {
//		Lock lock = getLock(group);
//		try {
//			lock.lock();
//			CompletableFuture<AjaxResult> lis = groupListernerCache.getIfPresent(group);
//			if (lis != null) {
//				if (lis.isDone()) {
//					return;
//				}
//				value.thenAccept(av -> {
//					if (av.getResult() instanceof FacadeResult) {
//						FacadeResult<?> fr = (FacadeResult<?>) av.getResult();
//						if (!fr.isExpected()) {
//							Throwable tw = fr.getException();
//							if (tw != null) {
//								if (tw instanceof ExceptionWrapper) {
//									tw = ((ExceptionWrapper) tw).getException();
//								}
//								AjaxErrorResult aer = AjaxErrorResult.exceptionResult(tw);
//								removeFuture(value);
//								lis.complete(aer);
//								return;
//							}
//							AjaxDataResult<?> arr = new AjaxDataResult<>();
//							arr.addObject(av);
//							removeFuture(value);
//							lis.complete(arr);
//							return;
//						}
//					}
//					AjaxDataResult<?> fr = new AjaxDataResult<>();
//					fr.addObject(av);
//					removeFuture(value);
//					lis.complete(fr);
//				}).exceptionally(throwable -> {
//					ExceptionUtil.logThrowable(logger, throwable);
//					removeFuture(value);
//					lis.complete(AjaxErrorResult.exceptionResult(throwable));
//					return null;
//				});
//			}
//			putToMap(group, key, value);
//			timeCostMap.put(value, new TimeElapsed());
//		} finally {
//			if (lock != null) {
//				lock.unlock();
//			}
//		}
//	}

	public SavedFuture getFuture(String group, Long id) {
		return sessionAndFutures.get(group).get(id);
	}
	
//	public CompletableFuture<AsyncTaskValue> getFuture(String group, String key) {
//		return sessionAndFutures.get(group).get(key);
//	}

//	public CompletableFuture<AsyncTaskValue> removeFuture(String group, String key) {
//		CompletableFuture<AsyncTaskValue> ca = sessionAndFutures.get(group).remove(key);
//		timeCostMap.remove(ca);
//		return ca;
//	}

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
	
//	public Map<String, CompletableFuture<AsyncTaskValue>> getFutureGroupMap(String group) {
//		return sessionAndFutures.get(group);
//	}
	
//	public List<FutureDetail> getFutureDetails(String group) {
//		Map<String, CompletableFuture<AsyncTaskValue>> myt = sessionAndFutures.get(group);
//		if (myt == null) {
//			return Lists.newArrayList();
//		}
//		return myt.entrySet().stream().map(es -> {
//			FutureDetail fd = new FutureDetail();
//			fd.setDescription(es.getKey());
//			fd.setDone(es.getValue().isDone());
//			fd.setExceptionally(es.getValue().isCompletedExceptionally());
//			fd.setTimeElapsed(timeCostMap.get(es.getValue()));
//			return fd;
//		}).collect(Collectors.toList());
//	}
	
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

//	public void removeFuture(CompletableFuture<AsyncTaskValue> it) {
//		for (Map<String, CompletableFuture<AsyncTaskValue>> map : sessionAndFutures.values()) {
//			Optional<Entry<String, CompletableFuture<AsyncTaskValue>>> ov = map.entrySet().stream().filter(es -> es.getValue() == it).findAny();
//			if (ov.isPresent()) {
//				map.remove(ov.get().getKey());
//				timeCostMap.remove(it);
//				break;
//			}
//		}
//	}
	
	public StoreState getStoreState() {
		StoreState ss = new StoreState();
		ss.setGroupListernerCache(groupListernerCache.asMap().size());
		ss.setLockCache(lockCache.asMap().size());
		ss.setSessionAndFutures(sessionAndFutures.values().stream().mapToInt(m -> m.size()).sum());
//		ss.setTimeCostMap(timeCostMap.size());
		return ss;
	}
	
	public class StoreState {
		private int groupListernerCache;
		private int sessionAndFutures;
//		private int timeCostMap;
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
//		public int getTimeCostMap() {
//			return timeCostMap;
//		}
//		public void setTimeCostMap(int timeCostMap) {
//			this.timeCostMap = timeCostMap;
//		}
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
					o = cf.get().getResult();
					if (o instanceof FacadeResult) {
						result = (((FacadeResult<?>) o).isExpected() + "").toUpperCase();
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
	
//	public static class FutureDetail {
//		private String description;
//		private boolean done;
//		
//		private boolean exceptionally;
//		
//		private TimeElapsed timeElapsed;
//		
//		public boolean isExceptionally() {
//			return exceptionally;
//		}
//
//		public void setExceptionally(boolean exceptionally) {
//			this.exceptionally = exceptionally;
//		}
//
//		public String getDescription() {
//			return description;
//		}
//
//		public void setDescription(String description) {
//			this.description = description;
//		}
//
//		public boolean isDone() {
//			return done;
//		}
//
//		public void setDone(boolean done) {
//			this.done = done;
//		}
//
//		public TimeElapsed getTimeElapsed() {
//			return timeElapsed;
//		}
//
//		public void setTimeElapsed(TimeElapsed timeElapsed) {
//			this.timeElapsed = timeElapsed;
//		}
//	}
	
//	public class TimeElapsed {
//		
//		private long id;
//		
//		private Instant startPoint;
//		
//		public TimeElapsed() {
//			this.startPoint = Instant.now();
//			this.id = GlobalStore.atomicLong.getAndIncrement();
//		}
//		
//		public String seconds() {
//			return String.valueOf((Instant.now().toEpochMilli() - startPoint.toEpochMilli()) / 1000);
//		}
//
//		public long getId() {
//			return id;
//		}
//
//		public void setId(long id) {
//			this.id = id;
//		}
//	}
}
