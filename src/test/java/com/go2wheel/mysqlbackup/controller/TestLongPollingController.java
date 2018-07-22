package com.go2wheel.mysqlbackup.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import com.go2wheel.mysqlbackup.SpringBaseTWithWeb;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.service.GlobalStore.Gobject;
import com.go2wheel.mysqlbackup.value.AjaxDataResult;

public class TestLongPollingController extends SpringBaseTWithWeb {
	
	@Autowired
	private GlobalStore globalStore;
	

	@Test
	public void testDeferredSuccess() throws Exception {

		MvcResult result = this.mockMvc.perform(get("/app/polling").param("sid", "123").accept(MediaType.APPLICATION_JSON_UTF8)).andReturn();
		
		assertThat(globalStore.getGroupListerners().size(), equalTo(1));
		
		CompletableFuture<AjaxDataResult> listener = globalStore.getGroupListerners().values().iterator().next();
		
		listener.thenAccept(o -> {
			assertThat(o.getData().size(), equalTo(1));
			assertThat(o.getData().get(0), equalTo("hello"));
		});
		
		// add a future task which take 2500 ms.
		globalStore.saveObject("123", "key", Gobject.newGobject("fc", CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
			}
			return "hello";
		})));

		this.mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk()).andDo(mr -> {
			MockHttpServletResponse r = mr.getResponse();
			byte[] bites = r.getContentAsByteArray();
			String s = new String(bites);
			System.out.println(s);
			JSONAssert.assertEquals(s, "{\"data\":[\"hello\"]}", JSONCompareMode.STRICT);
		});
	}
	
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
