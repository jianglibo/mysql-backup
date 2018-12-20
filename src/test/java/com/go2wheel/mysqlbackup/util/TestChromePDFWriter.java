package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.SpringBaseFort;

public class TestChromePDFWriter extends SpringBaseFort {

  @Autowired
  private ChromePdfWriter chromePdfWriter;

  @Autowired
  private MyAppSettings myAppSettings;

  @Test
  public void tBaidu() throws IOException, InterruptedException {
    // KeyValue kv =
    // keyValueDbService.findOneByKey(ChromePdfWriter.CHROME_EXECUTABLE_KEY);
    Path p = chromePdfWriter.writePdf("http://www.baidu.com");
    assertThat(Files.size(p), greaterThan(100L));
    System.out.println(p.toAbsolutePath().toString());
  }

}
