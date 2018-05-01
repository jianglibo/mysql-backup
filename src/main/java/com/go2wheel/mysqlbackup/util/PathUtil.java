package com.go2wheel.mysqlbackup.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PathUtil {
	
	public static String replaceDotWithSlash(String origin) {
		return origin.replace('.', '/');
	}
	
	public static String getExtWithoutDot(Path path) {
		String s = path.getFileName().toString();
		int p = s.lastIndexOf('.');
		if (p != -1) {
			return s.substring(p + 1);
		}
		return "";
	}
	
	private static String getZeros(int n) {
		char[] chars = new char[n];
		Arrays.fill(chars, '0');
		return new String(chars);
	}
	
	private static String prependZeros(int v, int length) {
		String si = v + "";
		int need = length - si.length();
		if (need > 0) {
			return getZeros(need) + si;
		} else {
			return si;
		}
	}
	
	public static Path getNextAvailable(Path file, int postfixNumber) {
		Path parent = file.getParent();
		String name = file.getFileName().toString();
		return getNextAvailable(parent, name, postfixNumber);
	}
	
	public static Path getNextAvailable(Path dir, String name, int postfixNumber) {
		Pattern ptn = Pattern.compile(String.format(".*%s\\.(\\d{%s})$", name, postfixNumber));
		List<String> paths = null;
		try {
			paths = Files.list(dir).filter(p -> ptn.matcher(p.toString()).matches()).map(p -> p.toString()).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(paths);
		
		if (paths.isEmpty()) {
			return dir.resolve(name + "." + getZeros(postfixNumber));
		} else {
			Matcher m = ptn.matcher(paths.get(paths.size() - 1));
			m.matches();
			String nm = m.group(1);
			int i = Integer.valueOf(nm);
			return dir.resolve(name + "." + prependZeros(++i, postfixNumber));
			
		}
	}
	
	public static void archiveLocalFile(Path origin, int postfixLength) {
		Path nextFn = getNextAvailable(origin.getParent(), origin.getFileName().toString(), postfixLength);
		try {
			Files.move(origin, nextFn, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Optional<Path> getJarLocation() {
		try {
			ProtectionDomain pd = PathUtil.class.getProtectionDomain();
			CodeSource cs = pd.getCodeSource();
			URL url = cs.getLocation();
			URI uri = url.toURI();
			String schemeSpecificPart = uri.getRawSchemeSpecificPart();
			
			if (!schemeSpecificPart.startsWith("file:")) {
				schemeSpecificPart = "file:" + schemeSpecificPart;
			}
			// jar:file:/D:/Documents/GitHub/mysql-backup/build/libs/mysql-backup-boot.jar!/BOOT-INF/classes!/
			// file:/D:/Documents/GitHub/mysql-backup/bin/main/
			int c = schemeSpecificPart.indexOf('!');
			if (c != -1) {
				schemeSpecificPart = schemeSpecificPart.substring(0, c);
			}
			File f = new File(new URL(schemeSpecificPart).toURI());
			return Optional.of(f.toPath().getParent());
		} catch (URISyntaxException | MalformedURLException e) {
			return Optional.empty();
		}
	}
}
