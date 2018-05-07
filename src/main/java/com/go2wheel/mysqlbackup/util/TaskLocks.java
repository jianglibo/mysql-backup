package com.go2wheel.mysqlbackup.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskLocks {
	
	public static final String TASK_MYSQL = "mysql";
	public static final String TASK_BORG = "borg";
	
	private static final Map<String, Lock> locks = new ConcurrentHashMap<>();
	
	
	public static synchronized Lock getBoxLock(String host, String taskType) {
		String key = host + taskType;
		if (locks.containsKey(key)) {
			return locks.get(key);
		} else {
			Lock lk = new ReentrantLock();
			locks.put(key, lk);
			return lk;
		}
	}
	
	
//	 class X {
//		   private final ReentrantLock lock = new ReentrantLock();
//		   // ...
//
//		   public void m() {
//		     lock.lock();  // block until condition holds
//		     try {
//		       // ... method body
//		     } finally {
//		       lock.unlock()
//		     }
//		   }
//		 }

}
