package com.go2wheel.mysqlbackup.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UpgradeUtil {

	public static final String UPGRADE_FLAG_FILE = "_upgrade.properties";
	
	public static final String BUILD_PROPERTIES_FILE = "BOOT-INF/classes/META-INF/build-info.properties";
	
	public static final Pattern JAR_FILE_PTN = Pattern.compile("mysql-backup-[^-]*-boot.jar");
	
	private static final Pattern MIGS_PTN = Pattern.compile(".*/mig/.*\\.yml");

	private final Path tmpPath;
	
	private String jarFile;
	
	private BuildInfo buildInfo;
	
	private SortedMap<String, String> migs = new TreeMap<>();

	public UpgradeUtil(Path zipFile) {
		this.tmpPath = extractFolder(zipFile);
		try {
			this.setBuildInfo(createBuildInfo());
		} catch (IOException e) {
		}
		iterateJarFile();
	}
	
	public UpgradeFile writeUpgradeFile() throws IOException {
		return writeUpgradeFile(Paths.get(""));
	}
	
	public UpgradeFile writeUpgradeFile(Path dir) throws IOException {
		Properties p = new Properties();
		p.setProperty(UpgradeFile.NEW_VESION, buildInfo.getVersion());
		p.setProperty(UpgradeFile.UPGRADE_JAR, tmpPath.resolve(jarFile).toAbsolutePath().toString());
		try (InputStream is = ClassLoader.class.getResourceAsStream("/META-INF/build-info.properties")) {
			if (is != null) {
				BuildInfo bi = new BuildInfo(is);
				p.setProperty(UpgradeFile.CURRENT_VESION, bi.getVersion());
			}
		}
		UpgradeFile uf = new UpgradeFile(p);
		if (uf.isUpgradeable()) {
			try (OutputStream os = Files.newOutputStream(dir.resolve(UPGRADE_FLAG_FILE))) {
				p.store(os, "upgrade file.");
			}
		}
		return uf;
	}
	
	public UpgradeFile getUpgradeFilẹ() throws IOException {
		Path upf = Paths.get(UPGRADE_FLAG_FILE);
		if (Files.exists(upf)) {
			return new UpgradeFile(upf);
		}
		return null;
	}

	private void iterateJarFile() {
		try (JarFile jfile = new JarFile(tmpPath.resolve(jarFile).toFile())) {
			Enumeration<?> zipFileEntries = jfile.entries();
			// Process each entry
			while (zipFileEntries.hasMoreElements()) {
				// grab a zip file entry
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				if (MIGS_PTN.matcher(currentEntry).matches()) {
					try (InputStream is = jfile.getInputStream(entry)) {
						String mig = StringUtil.inputstreamToString(is);
						migs.put(Paths.get(currentEntry).getFileName().toString(), mig);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BuildInfo createBuildInfo() throws IOException {
		if (this.tmpPath == null) {
			return null;
		}
		Path jarPath = tmpPath.resolve(jarFile);
		try (JarFile jfile = new JarFile(jarPath.toFile())) {
			JarEntry jentry = jfile.getJarEntry(BUILD_PROPERTIES_FILE);
			if (jentry == null) {
				return null;
			}
			try (InputStream is = jfile.getInputStream(jentry)) {
				return new BuildInfo(is);
			}
		}
	}
	
	private void printme(Object o) {
		System.out.println(o);
	}
	
	private Path extractFolder(Path zipFile) {
		int BUFFER = 2048;
		File file = zipFile.toFile();
		
		try (ZipFile zip = new ZipFile(file)) {
			Path newPath = Files.createTempDirectory("upgradeunzip");
			
			printme(newPath);

			Enumeration<?> zipFileEntries = zip.entries();

			// Process each entry
			while (zipFileEntries.hasMoreElements()) {
				// grab a zip file entry
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				
				if (JAR_FILE_PTN.matcher(currentEntry).matches()) {
					jarFile = currentEntry;
				}

				Path destFile = newPath.resolve(currentEntry);
				// destFile = new File(newPath, destFile.getName());
				Path destinationParent = destFile.getParent();

				// create the parent directory structure if needed
				Files.createDirectories(destinationParent);
				printme(currentEntry);
				if (!entry.isDirectory()) {
					try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry))) {
						int currentByte;
						// establish buffer for writing file
						byte data[] = new byte[BUFFER];

						// write the current file to disk
						try (OutputStream os = Files.newOutputStream(destFile)) {
							BufferedOutputStream dest = new BufferedOutputStream(os, BUFFER);
							// read and write until last byte is encountered
							while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
								dest.write(data, 0, currentByte);
							}
							dest.flush();
							dest.close();
							is.close();
						}
					}
				}

			}
			return newPath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public BuildInfo getBuildInfo() {
		return buildInfo;
	}


	public SortedMap<String, String> getMigs() {
		return migs;
	}

	public void setMigs(SortedMap<String, String> migs) {
		this.migs = migs;
	}



	public void setBuildInfo(BuildInfo buildInfo) {
		this.buildInfo = buildInfo;
	}
	
//	Properties p = new Properties();
//	p.setProperty("new-version", buildInfo.getVersion());
//	p.setProperty("upgrade-folder", tmpPath.resolve(jarFile).toAbsolutePath().toString());
//	try (InputStream is = ClassLoader.class.getResourceAsStream("/META-INF/build-info.properties")) {
//		if (is != null) {
//			BuildInfo bi = new BuildInfo(is);
//			p.setProperty("current-version", bi.getVersion());
//		} else {
//			p.setProperty("current-version", "0");
//		}
//	}
	
	public static class UpgradeFile {
		
		public static final String NEW_VESION = "new-version";
		public static final String CURRENT_VESION = "current-version";
		public static final String UPGRADE_JAR = "upgrade-jar";
		
		private Properties properties;
		
		public UpgradeFile(Properties properties) {
			this.properties = properties;
		}
		
		public boolean isUpgradeable() {
			return getNewVersion().compareTo(getCurrentVersion()) > 0;
		}

		
		public UpgradeFile() {
			properties = new Properties();
		}

		public UpgradeFile(InputStream is) throws IOException {
			loadis(is);
		}


		private void loadis(InputStream is) throws IOException {
			properties = new Properties();
			properties.load(is);
		}
		
		public UpgradeFile(Path upf) throws IOException {
			try (InputStream is = Files.newInputStream(upf)) {
				loadis(is);
			}
		}


		public String getCurrentVersion() {
			return properties.getProperty(CURRENT_VESION, "");
		}
		
		public String getNewVersion() {
			return properties.getProperty(NEW_VESION, "");
		}

		
		public String getUpgradeJar() {
			return properties.getProperty(UPGRADE_JAR, "");
		}


		
	}

	public static class BuildInfo {

		private Properties properties;

		public BuildInfo() {
			properties = new Properties();
		}

		public BuildInfo(InputStream is) throws IOException {
			properties = new Properties();
			properties.load(is);
		}

		public String getArtifact() {
			return properties.getProperty("build.artifact", "");
		}

		public String getGroup() {
			return properties.getProperty("build.group", "");
		}

		public String getName() {
			return properties.getProperty("build.name", "");
		}

		public String getVersion() {
			return properties.getProperty("build.version", "");
		}

		public String getTime() {
			return properties.getProperty("build.time", "");
		}

	}

}
