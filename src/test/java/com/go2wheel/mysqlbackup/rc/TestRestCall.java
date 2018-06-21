package com.go2wheel.mysqlbackup.rc;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.validator.internal.util.privilegedactions.GetInstancesFromServiceLoader;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.util.Md5Checksum;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class TestRestCall extends SpringBaseFort {

	private String url = "https://interface.zjzwfw.gov.cn/gateway/api/issue.htm?requestTime={requestTime}&sign={sign}&appKey={appKey}&issue={issue}";
	
	private String sec = "79046b69694244bf9eef3e3a4ac4ca88";
	private String appKey = "7d8ed6af56104c8bb6857e58ee08128a";

	private RestTemplate restTemplate;

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	@Before
	public void b() {
		this.restTemplate = restTemplateBuilder.build();
	}

	@Test
	public void someRestCall() {
		long d = Instant.now().toEpochMilli();

		Map<String, String> ms = new TreeMap<>();
		ms.put("requestTime", d + "");
		ms.put("sign", signed1(d));
		ms.put("appKey", appKey);

		String requestTime = d + "";
		String sign1 = signed1(d);
		String sign2 = signed2(d);

		// ResponseEntity<String> rs = this.restTemplate.postForEntity(url, null,
		// String.class, d, signed(d), "7d8ed6af56104c8bb6857e58ee08128a", 101);

		System.out.println(requestTime);
		System.out.println(appKey);
		System.out.println(sign1);
		System.out.println(sign2);
		// System.out.println(rs);
	}

	private String signed1(long d) {
		HashFunction hf = Hashing.md5();
		String s = appKey + sec + d;
		HashCode hc = hf.newHasher().putString(s, Charsets.UTF_8).hash();
		return hc.toString();
	}

	private String signed2(long d) {
		String s = appKey + sec + d;
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(s.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}
}
