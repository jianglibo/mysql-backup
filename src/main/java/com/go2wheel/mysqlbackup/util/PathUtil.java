package com.go2wheel.mysqlbackup.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
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
