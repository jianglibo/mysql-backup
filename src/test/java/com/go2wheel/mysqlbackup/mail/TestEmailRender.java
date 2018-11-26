package com.go2wheel.mysqlbackup.mail;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class TestEmailRender extends SpringBaseFort {
	
	@Autowired
	private EmailViewRender emailViewRender;
	
	private ServerGroupContext getsgc() throws IOException {
		Path pa = Paths.get("templates", "tplcontext.yml");
		String content = new String(Files.readAllBytes(pa), StandardCharsets.UTF_8);
		return YamlInstance.INSTANCE.yaml.loadAs(content, ServerGroupContext.class);
	}

	@Test()
	public void tThymeleafTplNoExt() throws IOException {
		String s = emailViewRender.render("ctx.ftl", getsgc());
		assertThat(s, equalTo("fdefault"));
		
		s = emailViewRender.render("ctx.html", getsgc());
		assertThat(s, containsString("<!DOCTYPE>"));

	}
}
