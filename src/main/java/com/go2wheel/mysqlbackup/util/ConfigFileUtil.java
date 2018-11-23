package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.value.ConfigFile;

public class ConfigFileUtil {
	
	protected static List<Path> getChildrenLatestFirst(Path dir) throws IOException {
		List<Path> children = Files.list(dir).collect(Collectors.toList());
		Collections.sort(children, new Comparator<Path>() {
			@Override
			public int compare(Path o1, Path o2) {
				return o2.getFileName().compareTo(o1.getFileName());
			}
		});
		return children;
	}
	
	protected static void pruneLog(Path dir, int numberToRemain) throws IOException {
		List<Path> children = getChildrenLatestFirst(dir);
		int len = children.size();
		int todeletenum = len - numberToRemain; 
		if (todeletenum < 0) {
			todeletenum = 0;
		} else if(todeletenum > len) {
			todeletenum = len;
		}
		int todeleteIdx = len - todeletenum;
		List<Path> todelete = children.subList(todeleteIdx, len);
		for(Path path: todelete) {
			Files.delete(path);
		}
	}
	
	
	public static List<Path> getLatestLogFiles(ConfigFile configFile, String actionKey, int num) throws IOException {
		Path p = configFile.getLogDirs().get(actionKey);
		List<Path> children = getChildrenLatestFirst(p);
		int n = num > children.size() ? children.size() : num;
		return children.subList(0, n);
	}
	
	public static void pruneLogs(ConfigFile configFile, int numberToRemain) throws IOException {
		for(Path dir: configFile.getLogDirs().values()) {
			pruneLog(dir, numberToRemain);
		}
	}
}
