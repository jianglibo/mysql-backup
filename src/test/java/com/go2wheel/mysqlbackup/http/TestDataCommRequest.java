package com.go2wheel.mysqlbackup.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.assertj.core.util.Lists;
import org.junit.Test;

import com.go2wheel.mysqlbackup.http.ZwfuCookieStore.ZwfuCookieStoreBuilder;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class TestDataCommRequest {
//	private String[] keys = new String[] { "query_cardId", "query_name", "fn", "serverComponentId" };

	// 129131536A0C438BA5605EDC312E3DA2 公安户籍信息
	// D63CBAAF87D89E432DB69E35D7437075 省公安厅居民身份证
	// 129131536A0C438BA5605EDC312E3DA2 婚姻信息
	
	private static List<Map<String, String>> queryMusts = Lists.newArrayList();
	
	private static String cardid = "330224197209163311";
	
	private String url = "http://10.68.130.194/ycslypt/SjgxAction.action";

	private String cookieDomain = "10.68.130.194";
	private String cookiePath = "/";

	private int c = 0;

	private String JsessionId = "A84D8A52C279DC9EE9E60C7B328079BE";

	
	static {
		Map<String, String> map = Maps.newHashMap();
		
		map.put("serverComponentId", "129131536A0C438BA5605EDC312E3DA2");
		map.put("query_cardId", cardid);
		map.put("fn", "commQuery");
		queryMusts.add(map);
		
		map = Maps.newHashMap();
		map.put("serverComponentId", "D63CBAAF87D89E432DB69E35D7437075");
		map.put("query_cardId", cardid);
		map.put("fn", "commQuery");
		queryMusts.add(map);
		
		map = Maps.newHashMap();
		map.put("serverComponentId", "129131536A0C438BA5605EDC312E3DA2");
		map.put("query_cardId", cardid);
		map.put("fn", "commQuery");
		queryMusts.add(map);

		
		map = Maps.newHashMap();
		map.put("serverComponentId", "9249B873EC9CF661799CD52D95E22047");
		map.put("query_cardId", cardid);
		map.put("fn", "commQuery");
		queryMusts.add(map);
		
		map = Maps.newHashMap();
		map.put("serverComponentId", "31606623F0903C8A4A361E362AC8BDEB");
		map.put("query_czrkgmsfhm", cardid);
		map.put("fn", "commQuery");
		queryMusts.add(map);
		
		
		map = Maps.newHashMap();
		map.put("serverComponentId", "5A8C22DF01782E24B62146F1F6D1612B");
		map.put("query_czrkgmsfhm", cardid);
		map.put("query_czrkxm", "江立波");
		map.put("fn", "commQuery");
		queryMusts.add(map);

	}
	



	public UrlEncodedFormEntity getFromEntity(Map<String, String> parameters) throws UnsupportedEncodingException {
		List<NameValuePair> urlParameters =  parameters.entrySet().stream()
				.map(es -> new BasicNameValuePair(es.getKey(), es.getValue()))
				.collect(Collectors.toList());
		return new UrlEncodedFormEntity(urlParameters);
	}


	@Test
	public void whenSettingCookiesOnTheHttpClient_thenCookieSentCorrectly()
			throws ClientProtocolException, IOException {
		ZwfuCookieStoreBuilder zsb = new ZwfuCookieStoreBuilder(cookieDomain, cookiePath);
		BasicCookieStore bcs = zsb.withSession(JsessionId).build();
		HttpClient client = HttpClientBuilder.create().setUserAgent("Mozilla/5.0 Firefox/26.0")
				.setDefaultCookieStore(bcs).build();

		for (Map<String, String> sid : queryMusts) {
			while (doOne(client, sid)) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean doOne(HttpClient client, Map<String, String> sid)
			throws UnsupportedEncodingException, IOException, ClientProtocolException {
		c++;
		final HttpPost post = new HttpPost(url);
		post.setEntity(getFromEntity(sid));

		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == 302) {
			System.out.println("请检查Session值�??");
			return false;

		}
		String content = new String(ByteStreams.toByteArray(response.getEntity().getContent()));
		if (!content.contains("\"code\":\"00\"")) {
			System.out.println(content);
			return false;
		}
		System.out.println(c);
		System.out.println(content);
		return true;
	}

}
