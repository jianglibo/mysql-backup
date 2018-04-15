package com.go2wheel.mysqlbackup.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class JarLocationGetter {

	public static File myLocation() throws URISyntaxException {
		ProtectionDomain pd = JarLocationGetter.class.getProtectionDomain();
		CodeSource cs = pd.getCodeSource();
		URL url = cs.getLocation();
		return new File(url.toURI().getPath());
	}
}
