package com.go2wheel.mysqlbackup.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import com.go2wheel.mysqlbackup.dbservice.GlobalStore;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.AjaxDataResult;
import com.go2wheel.mysqlbackup.value.AjaxErrorResult;
import com.go2wheel.mysqlbackup.value.AjaxResult;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;

@Controller
@RequestMapping("/app/polling")
public class LongPollingController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private GlobalStore globalStore;

	@ExceptionHandler
	public ResponseEntity<AjaxResult> handle(Exception ex) {
		if (ex instanceof AsyncRequestTimeoutException) {
			return ResponseEntity.ok(AjaxErrorResult.getTimeOutError());
		} else {
			return ResponseEntity.ok(new AjaxErrorResult(ex.getMessage()));
		}
	}

	@GetMapping("")
	@ResponseBody
	public CompletableFuture<AjaxResult> polling(@RequestParam(required = false) String sid,
			HttpServletRequest request) {
		String group = sid == null ? request.getSession(true).getId() : sid;

		Lock lock = globalStore.getLock(group);
		try {
			lock.lock();
			
			CompletableFuture<AjaxResult> cfInMap = globalStore.groupListernerCache.getIfPresent(group);
			
			if (cfInMap != null) {
				 if (cfInMap.isDone()) {
					 globalStore.groupListernerCache.invalidate(group);
				 }
				 return cfInMap;
			}

			List<CompletableFuture<AsyncTaskValue>> cfs = globalStore.getFutureGroupUnCompleted(group).stream().map(sf -> sf.getCf()).collect(Collectors.toList());
			// 只能对正在执行的异步作出响应，如果在等待的过程中有新的任务加入，必须在下一个循环中才能被测到?
//			CompletableFuture<?>[] cfs = lgo.stream().map(go -> go.as(CompletableFuture.class))
//					.toArray(size -> new CompletableFuture<?>[size]);

			CompletableFuture<AjaxResult> cf = new CompletableFuture<AjaxResult>();
			// 如果此CF正处于处于等待中，此时有新的任务加入，可以将将它加入监听中？
			globalStore.groupListernerCache.put(group, cf);

			CompletableFuture.anyOf(cfs.toArray(new CompletableFuture<?>[cfs.size()])).thenAccept(new Consumer<Object>() {
				@Override
				public void accept(Object t) {
					AjaxDataResult<AsyncTaskValue> ar = new AjaxDataResult<>();
					for (CompletableFuture<AsyncTaskValue> it : cfs) {
						AsyncTaskValue at = it.getNow(AsyncTaskValue.emptyValue());
						if (!at.isEmpty()) {
							globalStore.handleFutrueFacadeResult(at, cf);
						}
					}
					globalStore.groupListernerCache.invalidate(group);
					if (!cf.isDone()) {
						cf.complete(ar);						
					}
				}
			}).exceptionally(throwable -> { //如果发生意外，必须将发生意外的future移除。
				ExceptionUtil.logThrowable(logger, throwable);
				globalStore.groupListernerCache.invalidate(group);
				cf.complete(AjaxErrorResult.exceptionResult(throwable));
				return null;
			});
			
			return cf;
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}
}

/**
 * {"data":[{"description":"Dump MYSQL from 868.","result":{"expected":true,"result":{"pm":"-rw-r--r--.","numberOfLinks":1,"owner":"root","group":"root","size":658776,"lastModified":"Aug 10 08:16","filename":"/tmp/mysqldump.sql","md5":"6bddd3b712b1794b8020df5c5d392d91"},"commonActionResult":"DONE","exception":null,"message":null,"messagePlaceHolders":null,"startTime":0,"endTime":0},"empty":false}]}
 *
 * */
