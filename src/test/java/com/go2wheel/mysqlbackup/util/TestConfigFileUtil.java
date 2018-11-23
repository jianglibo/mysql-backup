package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestConfigFileUtil {
	
    @Rule
    public TemporaryFolder repofolder= new TemporaryFolder();
	
	@Test
	public void tGetChildrenLatestFirst() throws IOException {
		repofolder.newFile("20180916123344.log");
		repofolder.newFile("20180916123345.log");
		File f3 = repofolder.newFile("20181016123301.log");
		List<Path> children = ConfigFileUtil.getChildrenLatestFirst(repofolder.getRoot().toPath());
		assertThat(children.size(), equalTo(3));
		assertThat(children.get(0).getFileName().toString(), equalTo(f3.getName()));
		
	}
	
	@Test
	public void tPruneLog() throws IOException {
		repofolder.newFile("20180916123344.log");
		repofolder.newFile("20180916123345.log");
		File f3 = repofolder.newFile("20181016123301.log");
		ConfigFileUtil.pruneLog(repofolder.getRoot().toPath(), 1);
		
		List<Path> children = ConfigFileUtil.getChildrenLatestFirst(repofolder.getRoot().toPath());
		assertThat(children.size(), equalTo(1));
		assertThat(children.get(0).getFileName().toString(), equalTo(f3.getName()));
	}
	
	@Test
	public void tPruneLogExceed() throws IOException {
		repofolder.newFile("20180916123344.log");
		repofolder.newFile("20180916123345.log");
		repofolder.newFile("20181016123301.log");
		ConfigFileUtil.pruneLog(repofolder.getRoot().toPath(), 10);
		List<Path> children = ConfigFileUtil.getChildrenLatestFirst(repofolder.getRoot().toPath());
		assertThat(children.size(), equalTo(3));
	}
	
	@Test
	public void tPruneLogLess() throws IOException {
		repofolder.newFile("20180916123344.log");
		repofolder.newFile("20180916123345.log");
		repofolder.newFile("20181016123301.log");
		ConfigFileUtil.pruneLog(repofolder.getRoot().toPath(), 0);
		List<Path> children = ConfigFileUtil.getChildrenLatestFirst(repofolder.getRoot().toPath());
		assertThat(children.size(), equalTo(0));
	}

}
