package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.KeyValue;

public class TestChromePDFWriter extends SpringBaseFort {
	
	@Autowired
	private ChromePDFWriter chromePDFWriter;
	
	@Test
	public void tBaidu() throws IOException, InterruptedException {
		KeyValue kv = keyValueDbService.findOneByKey(ChromePDFWriter.CHROME_EXECUTABLE_KEY);
		Path p = chromePDFWriter.writePdf("http://www.baidu.com");
		assertThat(Files.size(p), greaterThan(100L));
		System.out.println(p.toAbsolutePath().toString());
	}

}
