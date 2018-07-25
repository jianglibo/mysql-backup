package com.go2wheel.mysqlbackup.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Test;

import com.google.common.io.ByteStreams;

public class TestCookie {
	
	private String url = "http://dw.zj.gov.cn/views/interfacebBox.html";
	
	private String domain = "dw.zj.gov.cn";
	private String cpath = "/";
	
	private BasicClientCookie createCooke(String key, String value) {
	    BasicClientCookie cookie = new BasicClientCookie(key, value);
	    cookie.setDomain(domain);
	    cookie.setPath(cpath);
	    return cookie;
	}

	@Test
	public void whenSettingCookiesOnTheHttpClient_thenCookieSentCorrectly() 
	  throws ClientProtocolException, IOException {
	    BasicCookieStore cookieStore = new BasicCookieStore();
	    
	    
	    cookieStore.addCookie(createCooke("JSESSIONID", "CD86266750C455D2E51FCD410398EBF3"));
	    cookieStore.addCookie(createCooke("OUTFOX_SEARCH_USER_ID_NCOO", "1762269608.8241184"));
	    cookieStore.addCookie(createCooke("___rl__test__cookies", "1529562974802"));
	    
//	    HttpClient instance = HttpClients.custom().setUserAgent("Mozilla/5.0 Firefox/26.0").build();
	    
	    HttpClient client = HttpClientBuilder.create().setUserAgent("Mozilla/5.0 Firefox/26.0").setDefaultCookieStore(cookieStore).build();
	 
	    final HttpGet request = new HttpGet(url);
	    request.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 Firefox/26.0");
	 
	    HttpResponse response = client.execute(request);
	    String content = new String(ByteStreams.toByteArray(response.getEntity().getContent()));
	    System.out.println(content);
	    assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
	}
	

	
	/*
	 * appKey	
2d0bcfb18a394f67b38404080f3e3add
endDate	
pageNum	
2
pageSize	
5
startDate	
http://dw.zj.gov.cn/web/myAppData.htm
	 */
}
