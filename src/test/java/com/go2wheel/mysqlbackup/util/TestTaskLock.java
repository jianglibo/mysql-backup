package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

public class TestTaskLock {
	
	@Test
	public void tDoubleAcquire() {
		Lock lock = TaskLocks.getBoxLock("abc", TaskLocks.TASK_FILEBACKUP);
		int i = 0;
		if (lock.tryLock()) {
			i++;
		}
		
		if (lock.tryLock()) {
			i++;
		}

		if (lock.tryLock()) {
			i++;
		}
		assertThat(i, equalTo(3));
	}
	
	@Test
	public void tDoubleAcquire1() {
		ReentrantLock lock = (ReentrantLock) TaskLocks.getBoxLock("abc", TaskLocks.TASK_FILEBACKUP);
		int i = 0;
		if (lock.tryLock()) {
			lock.lock();
			i++;
		}
		
		assertThat(lock.getHoldCount(), equalTo(2));
		lock.unlock();
		assertThat(lock.getHoldCount(), equalTo(1));
		lock.unlock();
		assertThat(lock.getHoldCount(), equalTo(0));
		assertThat(lock.getQueueLength(), equalTo(0));
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("a");
				Lock lock = TaskLocks.getBoxLock("abc", TaskLocks.TASK_FILEBACKUP);
				lock.lock();
				System.out.println("b");
				lock.unlock();
			}
		}).start();
		
		if (lock.tryLock()) {
			lock.lock();
			i++;
		}

		if (lock.tryLock()) {
			lock.lock();
			i++;
		}
		assertThat(i, equalTo(3));
	}


}
