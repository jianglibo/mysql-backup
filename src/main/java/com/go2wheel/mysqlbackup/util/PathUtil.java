package com.go2wheel.mysqlbackup.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathUtil {

	private static Logger logger = LoggerFactory.getLogger(PathUtil.class);

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

	/**
	 * if length was 3 and v was 1 then return 001.
	 * 
	 * @param v
	 * @param length
	 * @return
	 */
	private static String prependZeros(int v, int length) {
		String si = v + "";
		int need = length - si.length();
		if (need > 0) {
			return getZeros(need) + si;
		} else {
			return si;
		}
	}

	/**
	 * 
	 * @param fileOrDirToBackup
	 * @param postfixNumber
	 * @param roundNumber
	 *            when the number reaches, return to start.
	 * @return
	 */
	public static Path getNextAvailable(Path fileOrDirToBackup, int postfixNumber, int roundNumber) {
		Path parent = fileOrDirToBackup.getParent();
		String name = fileOrDirToBackup.getFileName().toString();
		return getNextAvailable(parent, name, postfixNumber, roundNumber);
	}

	public static Path getNextAvailable(Path fileOrDirToBackup, int postfixNumber) {
		Path parent = fileOrDirToBackup.getParent();
		String name = fileOrDirToBackup.getFileName().toString();
		return getNextAvailable(parent, name, postfixNumber);
	}

	/**
	 * 
	 * @param parentDir
	 * @param fileOrDirName
	 * @param postfixNumber
	 * @param roundNumberExclude
	 * @return
	 */
	protected static Path getNextAvailable(Path parentDir, String fileOrDirName, int postfixNumber,
			int roundNumberExclude) {
		Pattern ptn = Pattern.compile(String.format(".*%s\\.(\\d{%s})$", fileOrDirName, postfixNumber));
		List<String> paths = listVersionedFiles(parentDir, ptn);

		if (paths.isEmpty()) {
			return parentDir.resolve(fileOrDirName + "." + getZeros(postfixNumber));
		} else {
			Matcher m = ptn.matcher(paths.get(paths.size() - 1));
			m.matches();
			String nm = m.group(1);
			int i = Integer.valueOf(nm) + 1;
			if (i >= roundNumberExclude) {
				i = 0;
			}
			return parentDir.resolve(fileOrDirName + "." + prependZeros(i, postfixNumber));
		}
	}

	private static List<String> listVersionedFiles(Path parentDir, Pattern ptn) {
		List<String> paths = null;
		try {
			paths = Files.list(parentDir).filter(p -> ptn.matcher(p.toString()).matches()).map(p -> p.toString())
					.collect(Collectors.toList());
		} catch (IOException e) {
			ExceptionUtil.logErrorException(logger, e);
			return null;
		}
		Collections.sort(paths);
		return paths;
	}

	/**
	 * backup style is dump -> dump.000, if file goes dump.999, it should return
	 * 000.
	 * 
	 * @param parentDir
	 * @param fileOrDirName
	 * @param postfixNumber
	 * @return
	 */
	protected static Path getNextAvailable(Path parentDir, String fileOrDirName, int postfixNumber) {
		int roundNumber = (int) Math.pow(10, postfixNumber) - 1;
		return getNextAvailable(parentDir, fileOrDirName, postfixNumber, roundNumber);
	}

	public static void archiveLocalFile(Path origin, int postfixLength) {
		Path nextFn = getNextAvailable(origin.getParent(), origin.getFileName().toString(), postfixLength);
		try {
			Files.move(origin, nextFn, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Path getMaxVersion(Path originFileOrDir) throws IOException {
		int pf = guessPostfix(originFileOrDir);
		if (pf == -1) {
			return originFileOrDir;
		}
		Pattern ptn = Pattern.compile(String.format(".*%s\\.(\\d{%s})$", originFileOrDir.getFileName().toString(), pf));
		List<String> s = listVersionedFiles(originFileOrDir.getParent(), ptn);
		if (s.size() > 0) {
			return Paths.get(s.get(s.size() - 1));
		} else {
			return null;
		}
	}
	
	private static int guessPostfix(Path originFileOrDir) throws IOException {
		String fn = originFileOrDir.getFileName().toString();
		Pattern ptn = Pattern.compile(fn + "\\.(\\d+)$");
		Map<Object, Integer> postfix = Files.list(originFileOrDir.getParent()).map(p -> p.getFileName().toString())
				.map(f -> {
					Matcher m = ptn.matcher(f);
					if (m.matches()) {
						return m.group(1).length();
					} else {
						return 0;
					}
				}).filter(i -> i > 0).collect(Collectors.toMap(i -> i, i -> i, (Integer s, Integer a) -> {
					return a;
				}));
		if (postfix.size() == 1) {
			return postfix.values().iterator().next();
		}
		return  -1;
	}

	public static Path getNextAvailableByBaseName(Path originFileOrDir, int postfixHint) throws IOException {
		int pf = guessPostfix(originFileOrDir);
		if (pf == -1) {
			pf = postfixHint;
		}
		return getNextAvailable(originFileOrDir, pf);
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
