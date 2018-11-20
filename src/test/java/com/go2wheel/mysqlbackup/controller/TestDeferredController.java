package com.go2wheel.mysqlbackup.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.request.async.DeferredResult;

import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;
import com.go2wheel.mysqlbackup.dbservice.GlobalStore;

public class TestDeferredController extends SpringBaseTWithWeb {
	
	@Autowired
	private GlobalStore globalStore;
	

//	@Test
//	public void testDeferredSuccess() throws Exception {
//
//		MvcResult result = this.mockMvc.perform(get("/quotes").param("timeout", "3000")).andReturn();
//		
//		DeferredResult<String> df = globalStore.removeFuture("test", "http").as(DeferredResult.class);
//		
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(2500);
//					df.setResult("hello js.");
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//			}
//		}).start();
//
//		this.mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk())
//				.andExpect(content().string("hello js."));
//	}
	
//	@Test(expected=IllegalStateException.class)
//	public void testDeferredTimeout() throws Exception {
//
//		MvcResult result = this.mockMvc.perform(get("/quotes").param("timeout", "1000")).andReturn();
//		
//		DeferredResult<String> df = globalStore.removeObject("test", "http").as(DeferredResult.class);
//		
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(2500);
//					if(!df.isSetOrExpired()) {
//						df.setResult("hello js.");
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//			}
//		}).start();
//
//		this.mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk())
//				.andExpect(content().string("hello js."));
//	}


}
