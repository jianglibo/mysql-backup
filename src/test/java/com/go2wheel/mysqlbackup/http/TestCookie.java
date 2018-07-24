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
	{"res":1,"resMsg":"数据获取成功","obj":{"appVo":[{"applyId":null,"appName":"宁波市统一受理平台","appKey":"2d0bcfb18a394f67b38404080f3e3add"
		,"interfaceName":null,"interfaceCode":null,"applyStatus":null,"transferDataCount":0,"accessCount":0,"maxAccessCount"
		:0,"maxDataCount":0,"remaindAccessCount":0,"remaindDataCount":0,"startDate":null,"endDate":null,"accessDate"
		:null,"count":null,"datecount":null},{"applyId":null,"appName":"奉化区权利运行系统","appKey":"7d8ed6af56104c8bb6857e58ee08128a"
		,"interfaceName":null,"interfaceCode":null,"applyStatus":null,"transferDataCount":0,"accessCount":0,"maxAccessCount"
		:0,"maxDataCount":0,"remaindAccessCount":0,"remaindDataCount":0,"startDate":null,"endDate":null,"accessDate"
		:null,"count":null,"datecount":null}],"applist":[{"applyId":null,"appName":"宁波市统一受理平台","appKey":"2d0bcfb18a394f67b38404080f3e3add"
		,"interfaceName":null,"interfaceCode":null,"applyStatus":null,"transferDataCount":0,"accessCount":666
		,"maxAccessCount":0,"maxDataCount":0,"remaindAccessCount":-666,"remaindDataCount":0,"startDate":null
		,"endDate":null,"accessDate":null,"count":0,"datecount":0}],"listdep":[{"id":null,"interfaceCode":"PopulationInfo"
		,"interfaceName":"人口信息","interfaceDepartment":null,"departmentName":null,"useDepartment":null,"accessCount"
		:563,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate":null,"formatDate"
		:null,"status":0,"startDate":null,"endDate":null,"appKey":null},{"id":null,"interfaceCode":"bnpmrbd3c4945G03"
		,"interfaceName":"省公安厅居民户口簿（个人）","interfaceDepartment":null,"departmentName":null,"useDepartment":null
		,"accessCount":3,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate":null
		,"formatDate":null,"status":0,"startDate":null,"endDate":null,"appKey":null},{"id":null,"interfaceCode"
		:"PubSecDeptPopInfo","interfaceName":"公安户籍信息","interfaceDepartment":null,"departmentName":null,"useDepartment"
		:null,"accessCount":2,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate"
		:null,"formatDate":null,"status":0,"startDate":null,"endDate":null,"appKey":null},{"id":null,"interfaceCode"
		:"2s3sb9do8S3VDaj5","interfaceName":"省公安厅居民身份证","interfaceDepartment":null,"departmentName":null,"useDepartment"
		:null,"accessCount":2,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate"
		:null,"formatDate":null,"status":0,"startDate":null,"endDate":null,"appKey":null},{"id":null,"interfaceCode"
		:"3sb8c6d027ZhDW04","interfaceName":"省公安厅居民身份证（新）","interfaceDepartment":null,"departmentName":null,"useDepartment"
		:null,"accessCount":1,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate"
		:null,"formatDate":null,"status":0,"startDate":null,"endDate":null,"appKey":null},{"id":null,"interfaceCode"
		:"SecurityGuardInfo","interfaceName":"保安员信息","interfaceDepartment":null,"departmentName":null,"useDepartment"
		:null,"accessCount":1,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate"
		:null,"formatDate":null,"status":0,"startDate":null,"endDate":null,"appKey":null}],"list":{"pageNum"
		:1,"pageSize":5,"totalRecord":7,"totalPage":2,"results":[{"id":null,"interfaceCode":null,"interfaceName"
		:null,"interfaceDepartment":"001003007","departmentName":"省公安厅","useDepartment":null,"accessCount":572
		,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate":null,"formatDate":null
		,"status":0,"startDate":null,"endDate":null,"appKey":null},{"id":null,"interfaceCode":null,"interfaceName"
		:null,"interfaceDepartment":"001003033","departmentName":"省工商局","useDepartment":null,"accessCount":84
		,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate":null,"formatDate":null
		,"status":0,"startDate":null,"endDate":null,"appKey":null},{"id":null,"interfaceCode":null,"interfaceName"
		:null,"interfaceDepartment":"001003085","departmentName":"省人力社保厅","useDepartment":null,"accessCount"
		:4,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate":null,"formatDate":null
		,"status":0,"startDate":null,"endDate":null,"appKey":null},{"id":null,"interfaceCode":null,"interfaceName"
		:null,"interfaceDepartment":"001003003","departmentName":"省卫生计生委","useDepartment":null,"accessCount"
		:3,"beAccessdCount":0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate":null,"formatDate":null
		,"status":0,"startDate":null,"endDate":null,"appKey":null},{"id":null,"interfaceCode":null,"interfaceName"
		:null,"interfaceDepartment":"001021001","departmentName":"省高院","useDepartment":null,"accessCount":1,"beAccessdCount"
		:0,"transferDataCount":0,"beTransferdDataCount":0,"accessDate":null,"formatDate":null,"status":0,"startDate"
		:null,"endDate":null,"appKey":null}]}}}
		*/
	
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
