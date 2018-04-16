package com.go2wheel.mysqlbackup.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Optional;

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
	
	public static Optional<Path> getJarLocation() {
		ProtectionDomain pd = PathUtil.class.getProtectionDomain();
		CodeSource cs = pd.getCodeSource();
		URL url = cs.getLocation();
		try {
			return Optional.of(new File(url.toURI().getPath()).toPath());
		} catch (URISyntaxException e) {
			return Optional.empty();
		}
	}
}
