package com.go2wheel.mysqlbackup.expect.future;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;

import org.junit.Test;

public class TestFuture {
	
	
	@Test
	public void testDependOnNewCreated() {
		CompletableFuture<String> cf = new CompletableFuture<String>();
		
		cf.exceptionally(th -> {
			System.out.println(th);
			return null;
		});
		Thread t = Thread.currentThread();
		
		CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			return "work";
		});
	
		CompletableFuture.anyOf(cf1).thenRun(
				new Runnable() {
			@Override
			public void run() {
				boolean b = cf.complete("hello");
				assertTrue(b);
				b = cf.complete("hello");
				assertTrue(!b);
				b = cf.complete("hello");
				assertTrue(!b);
				assertTrue(cf.isDone());
				t.interrupt();
			}
		}).exceptionally(th -> {
			System.out.println(th);
			t.interrupt();
			return null;
		});
		
		try {
			Thread.sleep(200000);
		} catch (InterruptedException e) {
		}
	}

}
