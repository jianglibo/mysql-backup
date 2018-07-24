package com.go2wheel.mysqlbackup.http;

import java.util.List;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.google.common.collect.Lists;

public class ZwfuCookieStore {
	
	public static class ZwfuCookieStoreBuilder {
		
		private final String cookieDomain;
		private final String cookiePath;
		
		private List<BasicClientCookie> cookies = Lists.newArrayList();
		
		public ZwfuCookieStoreBuilder(String cookieDomain, String cookiePath) {
			this.cookieDomain = cookieDomain;
			this.cookiePath = cookiePath;
		}
		
		public String getCookieDomain() {
			return cookieDomain;
		}
		public String getCookiePath() {
			return cookiePath;
		}
		
		public ZwfuCookieStoreBuilder withSession(String session) {
			return withCookie("JSESSIONID", session);
		}
		
		public ZwfuCookieStoreBuilder withCookie(String key, String value) {
		    BasicClientCookie cookie = new BasicClientCookie(key, value);
		    cookie.setDomain(cookieDomain);
		    cookie.setPath(cookiePath);
		    cookies.add(cookie);
		    return this;
		}
		
		public BasicCookieStore build() {
			BasicCookieStore cookieStore = new BasicCookieStore();
			cookieStore.addCookies(cookies.stream().toArray(size -> new BasicClientCookie[size]));
			return cookieStore;
		}
		
	}

}
