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
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.exception.ShowToUserException;
import com.go2wheel.mysqlbackup.value.CommonFileNames;

public class UpgradeUtil {

	private static Logger logger = LoggerFactory.getLogger(UpgradeUtil.class);

	public static final String UPGRADE_FLAG_FILE = "_upgrade.properties";

	public static final String BUILD_PROPERTIES_FILE = "BOOT-INF/classes/META-INF/build-info.properties";

	public static final Pattern JAR_FILE_PTN = Pattern.compile("mysql-backup-[^-]*-boot.jar");

	private static final Pattern MIGS_PTN = Pattern.compile(".*/mig/.*\\.yml");

	private final Path tmpPath;

	private String jarFile;

	private BuildInfo buildInfo;

	private SortedMap<String, String> migs = new TreeMap<>();

	public UpgradeUtil(Path zipFile) throws IOException {
		if (zipFile != null) {
			this.tmpPath = extractFolder(zipFile);
			this.setBuildInfo(createBuildInfo());
			iterateJarFile();
		} else {
			this.tmpPath = null;
		}
	}

	public UpgradeFile writeUpgradeFile() throws IOException {
		return writeUpgradeFile(Paths.get(""));
	}

	public UpgradeFile writeUpgradeFile(Path dir) throws IOException {
		return writeUpgradeFile(dir, buildInfo);
	}

	public UpgradeFile writeUpgradeFile(Path dir, BuildInfo buildInfo) throws IOException {
		Properties p = new Properties();
		p.setProperty(UpgradeFile.NEW_VESION, buildInfo.getVersion());
		p.setProperty(UpgradeFile.UPGRADE_JAR, tmpPath.resolve(jarFile).toAbsolutePath().toString());
		String f = tmpPath.toAbsolutePath().toString();
		if (!f.endsWith("\\")) {
			f = f + "\\";
		}
		p.setProperty(UpgradeFile.UPGRADE_FOLDER, f);
		
		InputStream is = null;
		try {
			is = ClassLoader.class.getResourceAsStream("/META-INF/build-info.properties");
			if (is == null) {
				is = ClassLoader.class.getResourceAsStream("/BOOT-INF/classes/META-INF/build-info.properties");
			}
			if (is != null) {
				BuildInfo bi = new BuildInfo(is);
				p.setProperty(UpgradeFile.CURRENT_VESION, bi.getVersion());
			} else {
				throw new ShowToUserException("upgrade.cannotdetermineversion");
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
		
		UpgradeFile uf = new UpgradeFile(p);
		if (uf.isUpgradeable()) {
			List<String> lines = p.entrySet().stream().map(et -> et.getKey() + "=" + et.getValue())
					.collect(Collectors.toList());
			Files.write(dir.resolve(UPGRADE_FLAG_FILE), lines);
		}
		return uf;
	}

	public UpgradeFile getUpgradeFilẹ() throws IOException {
		return getUpgradeFilẹ(Paths.get(""));
	}

	public UpgradeFile getUpgradeFilẹ(Path dir) throws IOException {
		Path upf = dir.resolve((UPGRADE_FLAG_FILE));
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

	public static class UpgradeFile {

		public static final String NEW_VESION = "new-version";
		public static final String CURRENT_VESION = "current-version";
		public static final String UPGRADE_JAR = "upgrade-jar";
		public static final String UPGRADE_FOLDER = "upgrade-folder";

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
			} catch (Exception e) {
				Files.readAllLines(upf).stream().map(line -> line.trim()).map(line -> line.split("=", 2))
						.filter(pair -> pair.length == 2).forEach(pa -> {
							properties.put(pa[0], pa[1]);
						});
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

		public String getUpgradeFolder() {
			return properties.getProperty(UPGRADE_FOLDER, "");
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

	public static void doUpgrade(Path curPath, String[] args) throws IOException {
		// --spring.datasource.url=jdbc:hsqldb:file:%wdirslash%%_db%;shutdown=true
		curPath = curPath.toAbsolutePath();
		if (!Files.exists(Paths.get(UpgradeUtil.UPGRADE_FLAG_FILE))) {
			logger.info("no upgrade file {} found. skiping.",
					Paths.get(UpgradeUtil.UPGRADE_FLAG_FILE).toAbsolutePath().toString());
			return;
		}
		UpgradeFile uf = new UpgradeFile(curPath.resolve(UpgradeUtil.UPGRADE_FLAG_FILE));
		Path newJar = Paths.get(uf.getUpgradeJar());
		String newVersion = uf.getNewVersion();

		Path zipPath = Paths.get(uf.getUpgradeFolder());

		if (!Files.exists(zipPath)) {
			logger.error("zipFolder doesn't exits! {}", zipPath.toString());
			return;
		}

		logger.info("start upgrading...");
		Pattern dbPathPtn = Pattern.compile(".*jdbc:hsqldb:file:([^;]+);.*");
		String dbPath = null;
		Optional<Path> currentJarOp = Files.list(curPath)
				.filter(p -> UpgradeUtil.JAR_FILE_PTN.matcher(p.getFileName().toString()).matches()).findAny();
		if (!currentJarOp.isPresent()) {
			logger.error("Cannot locate current jar file.");
			return;
		}
		Path currentJar = currentJarOp.get();

		for (String s : args) {
			Matcher m = dbPathPtn.matcher(s);
			if (m.matches()) {
				dbPath = m.group(1);
			}
		}

		if (dbPath == null) {
			logger.info("Cannot find db path.");
			return;
		} else {
			// This pattern is fixed.
			logger.info("db path: {}", dbPath);
			if (!Files.exists(Paths.get(dbPath))) {
				logger.info("Find db path {}. But doesn't exists.", dbPath);				
			}
		}
		
		Path dbDir = Paths.get(dbPath).getParent();
		doChange(curPath, zipPath, currentJar, newJar, dbDir, newVersion);
		
    	Path upgrade = curPath.resolve(UpgradeUtil.UPGRADE_FLAG_FILE);
    	if (Files.exists(upgrade)) {
    		try {
				Files.delete(upgrade);
				System.exit(BackupCommand.RESTART_CODE);
			} catch (IOException e) {
				ExceptionUtil.logErrorException(logger, e);
			}
    	}
	}

	protected static void doChange(Path curPath, Path unZippedPath, Path currentJar, Path newJar, Path dbDir,
			String newVersion) throws IOException {
		backupDb(dbDir);
		backupProperties(curPath, unZippedPath, newVersion);
		backupBat(curPath, unZippedPath);
		backupJar(curPath, currentJar, newJar);
	}

	private static void backupJar(Path curPath, Path currentJar, Path newJar) throws IOException {
		Path bak = currentJar.getParent().resolve(currentJar.getFileName().toString() + ".prev");
		Files.move(currentJar, bak);
		Files.copy(newJar, curPath.resolve(newJar.getFileName()));
	}

	private static void backupBat(Path curPath, Path unZippedPath) throws IOException {
		Path curBat = curPath.resolve(CommonFileNames.START_BATCH);
		Path newBat = unZippedPath.resolve(CommonFileNames.START_BATCH);
		FileUtil.backup(3, false, curBat);
		Files.copy(newBat, curBat);
	}

	private static void backupProperties(Path curPath, Path unZippedPath, String newVersion) throws IOException {
		Path currentApplicationProperties = curPath.resolve(CommonFileNames.APPLICATION_CONFIGURATION);
		Properties pros = new Properties();
		Properties npros = new Properties();
		try (InputStream is = Files.newInputStream(currentApplicationProperties);
				InputStream isn = Files.newInputStream(unZippedPath.resolve(CommonFileNames.APPLICATION_CONFIGURATION))) {
			pros.load(is);
			npros.load(isn);
			npros.putAll(pros);
		}
		FileUtil.backup(3, true, currentApplicationProperties);
		try (OutputStream os = Files.newOutputStream(currentApplicationProperties)) {
			npros.store(os, newVersion);
		}
	}

	private static void backupDb(Path dbPath) throws IOException {
		FileUtil.backup(3, true, dbPath);
	}

}
