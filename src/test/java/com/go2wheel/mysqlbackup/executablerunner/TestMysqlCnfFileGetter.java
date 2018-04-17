package com.go2wheel.mysqlbackup.executablerunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.value.ExternalExecuteResult;

public class TestMysqlCnfFileGetter {
	
	@Test
	public void t() throws IOException {
		MysqlCnfFileFinder mcfg = new MysqlCnfFileFinder();
		ExternalExecuteResult<List<String>> cnfs = mcfg.execute();
		
		Path f = cnfs.getResult().stream().map(Paths::get).filter(Files::exists).findFirst().get();
		
		Files.lines(f).forEach(System.out::println);
	}

}
