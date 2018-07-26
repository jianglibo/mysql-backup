package com.go2wheel.mysqlbackup.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import com.go2wheel.mysqlbackup.http.ZwfuCookieStore.ZwfuCookieStoreBuilder;
import com.google.common.io.ByteStreams;

public class TestDataRequest {

	public static class RequestFormData {
		
		private String fn = "query";
		private String serverComponentId = "9249B873EC9CF661799CD52D95E22047";
		private String idcard = "330224197209163311";
		private String table = "pop";
		
		public RequestFormData() {
			
		}
		
		public RequestFormData(String idcard) {
			this.idcard = idcard;
		}


		public String getFn() {
			return fn;
		}

		public void setFn(String fn) {
			this.fn = fn;
		}

		public String getServerComponentId() {
			return serverComponentId;
		}

		public void setServerComponentId(String serverComponentId) {
			this.serverComponentId = serverComponentId;
		}

		public String getIdcard() {
			return idcard;
		}

		public void setIdcard(String idcard) {
			this.idcard = idcard;
		}

		public String getTable() {
			return table;
		}

		public void setTable(String table) {
			this.table = table;
		}
		
		public UrlEncodedFormEntity getFromEntity() throws UnsupportedEncodingException {
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("fn", getFn()));
			urlParameters.add(new BasicNameValuePair("serverComponentId", getServerComponentId()));
			urlParameters.add(new BasicNameValuePair("idcard", getIdcard()));
			urlParameters.add(new BasicNameValuePair("table", getTable()));
			return new UrlEncodedFormEntity(urlParameters);
		}
	}

	private String url = "http://10.68.130.194/ycslypt/SjgxAction.action";

	private String cookieDomain = "10.68.130.194";
	private String cookiePath = "/";
	
	private int c = 0;
	
	private String JsessionId = "B78E4F219509BB372C0AF0DC5F37A620";


	@Test
	public void whenSettingCookiesOnTheHttpClient_thenCookieSentCorrectly()
			throws ClientProtocolException, IOException {
		ZwfuCookieStoreBuilder zsb = new ZwfuCookieStoreBuilder(cookieDomain, cookiePath);
		BasicCookieStore bcs = zsb.withSession(JsessionId).build();
		HttpClient client = HttpClientBuilder.create().setUserAgent("Mozilla/5.0 Firefox/26.0")
				.setDefaultCookieStore(bcs).build();
		
		while(doOne(client)) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	private boolean doOne(HttpClient client)
			throws UnsupportedEncodingException, IOException, ClientProtocolException {
		c++;
		final HttpPost post = new HttpPost(url);
		post.setEntity(new RequestFormData().getFromEntity());

		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == 302) {
			System.out.println("请检查Session值�??");
			return false;
			
		}
		String content = new String(ByteStreams.toByteArray(response.getEntity().getContent()));
		if (content.contains("\"code\":\"18\"")) {
			System.out.println("we get the daily limit.");
			return false;
		}
		System.out.println(c);
		System.out.println(content);
		return true;
	}

}
