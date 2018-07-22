package com.go2wheel.mysqlbackup.util;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class FutureUtil {
	
	public static <T> CompletableFuture<T> failAfter(Duration duration) {
	    final CompletableFuture<T> promise = new CompletableFuture<>();
	    scheduler.schedule(() -> {
	        final TimeoutException ex = new TimeoutException("Timeout after " + duration);
	        return promise.completeExceptionally(ex);
	    }, duration.toMillis(), TimeUnit.MILLISECONDS);
	    return promise;
	}
	 
	private static final ScheduledExecutorService scheduler =
	        Executors.newScheduledThreadPool(
	                1,
	                new ThreadFactoryBuilder()
	                        .setDaemon(true)
	                        .setNameFormat("failAfter-%d")
	                        .build());
	
	public static <T> CompletableFuture<T> within(CompletableFuture<T> future, Duration duration) {
	    final CompletableFuture<T> timeout = failAfter(duration);
	    return future.applyToEither(timeout, Function.identity());
	}

}
