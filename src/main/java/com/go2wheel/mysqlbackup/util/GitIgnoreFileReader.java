package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitIgnoreFileReader {
	
	public static List<PathMatcher> ignoreMatchers(Path igfile) {
		try (Stream<String> lines = Files.lines(igfile)) {
			return calListOfMatchers(lines);
		} catch (IOException e) {
			return new ArrayList<>();
		}
	}

	private static List<PathMatcher> calListOfMatchers(Stream<String> lines) {
		return lines.map(line -> line.trim()).filter(line -> !line.startsWith("#")).map(line -> line.replace('\\', '/')).flatMap(line -> {
			List<String> ls = new ArrayList<>();
			if (line.startsWith("/")) {
				line = line.substring(1);
			}
			
			if (line.startsWith("**/")) {
				ls.add(line.substring(3));
			}
			ls.add(line);
			
			ls = ls.stream().flatMap(al -> {
				List<String> newLines = new ArrayList<>();
				if (al.endsWith("/")) {
					newLines.add(al.substring(0, al.length() - 1));
					newLines.add(al + "**");
				}
				newLines.add(al);
				return newLines.stream();
			}).collect(Collectors.toList());

			ls.sort(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
			return ls.stream();
		}).distinct().map(line -> "glob:" + line).map(FileSystems.getDefault()::getPathMatcher).collect(Collectors.toList());
	}
	
	public static List<PathMatcher> ignoreMatchers(List<String> lines) {
		return calListOfMatchers(lines.stream());
	}
}
