package com.go2wheel.mysqlbackup.yml;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

public class TestTasteYml {
	
	@Test
	public void t() throws IOException {
		Yaml yaml = new Yaml();
		MysqlInstance instance =  yaml.loadAs(Files.newInputStream(UtilForTe.getMysqlInstanceDescription("localhost")), MysqlInstance.class);
		assertThat(instance.getHost(), equalTo("localhost"));
		assertThat(instance.getPort(), equalTo(3306));
		assertThat(instance.getUsername(), equalTo("root"));
		assertThat(instance.getPassword(), equalTo("123456"));
		
		String s = yaml.dump(instance);
		System.out.println(s);
		
		s = yaml.dumpAsMap(instance);
		System.out.println(s);
		
	}

}
