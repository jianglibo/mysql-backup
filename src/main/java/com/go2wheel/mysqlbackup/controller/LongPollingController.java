package com.go2wheel.mysqlbackup.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.GlobalStore.Gobject;
import com.go2wheel.mysqlbackup.value.AjaxDataResult;
import com.go2wheel.mysqlbackup.value.AjaxError;

@Controller
@RequestMapping("/app/polling")
public class LongPollingController {

	@Autowired
	private GlobalStore globalStore;

	@ExceptionHandler
	public ResponseEntity<AjaxError> handle(Exception ex) {
		if (ex instanceof AsyncRequestTimeoutException) {
			return ResponseEntity.ok(AjaxError.getTimeOutError());
		} else {
			return ResponseEntity.ok(new AjaxError(ex.getMessage()));
		}
	}

	@GetMapping("")
	@ResponseBody
	public CompletableFuture<AjaxDataResult> polling(@RequestParam(required = false) String sid,
			HttpServletRequest request) {
		String group = sid == null ? request.getSession(true).getId() : sid;

		Lock lock = globalStore.getLock(group);
		try {
			lock.lock();
			List<Gobject> lgo = globalStore.getGroupObjects(group);

			// 只能对正在执行的异步作出响应，如果在等待的过程中有新的任务加入，必须在下一个循环中才能检测到。
			CompletableFuture<?>[] cfs = lgo.stream().map(go -> go.as(CompletableFuture.class))
					.toArray(size -> new CompletableFuture<?>[size]);

			CompletableFuture<AjaxDataResult> cf = new CompletableFuture<AjaxDataResult>();
			// 如果此CF正处于处于等待中，此时有新的任务加入，可以将将它加入监听中。
			globalStore.addListerner(group, cf);

			CompletableFuture.anyOf(cfs).thenAccept(new Consumer<Object>() {
				@Override
				public void accept(Object t) {
					AjaxDataResult fr = new AjaxDataResult();
					for (CompletableFuture<?> it : cfs) {
						Object o = it.getNow(null);
						if (o != null) {
							fr.addObject(o);
							globalStore.removeObject(it);
						}
					}
					cf.complete(fr);
				}
			});
			return cf;
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}
}
