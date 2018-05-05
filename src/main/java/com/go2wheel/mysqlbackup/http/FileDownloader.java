package com.go2wheel.mysqlbackup.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.util.StringUtil;

@Component
public class FileDownloader {
	
	private MyAppSettings appSettings;
	
	private CloseableHttpClient httpclient;
	
	
	@PostConstruct
	public void post() {
		httpclient = HttpClients.custom()
		        .setDefaultRequestConfig(RequestConfig.custom()
		                .setCookieSpec(CookieSpecs.STANDARD).build())
		            .build();
	}
	
	public Path download(String url) throws ClientProtocolException, IOException {
		Path out = appSettings.getDownloadRoot().resolve(StringUtil.getLastPartOfUrl(url));
		if (Files.exists(out)) {
			return out;
		}
		HttpGet httpget = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(httpget);
		try {
		    HttpEntity entity = response.getEntity();
		    if (entity != null) {
		        InputStream instream = entity.getContent();
		        OutputStream os = null;
		        try {
		        	os = new BufferedOutputStream(Files.newOutputStream(out));
		            byte[] buffer = new byte[1024];
		            int bytesRead;
		            //read from is to buffer
		            while((bytesRead = instream.read(buffer)) != -1){
		                os.write(buffer, 0, bytesRead);
		            }
		            os.flush();
		        } finally {
		            instream.close();
		            if (os != null) {
		            	os.close();
		            }
		        }
		    }
		    return out;
		} finally {
		    response.close();
		}
	}
	
	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

}
