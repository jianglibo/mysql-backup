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
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.value.CommonFileNames;

public class UpgradeUtil {

	private static Logger logger = LoggerFactory.getLogger(UpgradeUtil.class);

	public static final String UPGRADE_FLAG_FILE = "_upgrade.properties";

	public static final String APP_VERSION_PROPERTIES_FILE_1 = "BOOT-INF/classes/app.version";
	public static final String APP_VERSION_PROPERTIES_FILE_2 = "app.version";
	public static final String APP_VERSION_PROPERTIES_FILE_3 = "/BOOT-INF/classes/app.version";
	public static final String APP_VERSION_PROPERTIES_FILE_4 = "/app.version";

	public static final Pattern JAR_FILE_PTN = Pattern.compile("mysql-backup-[^-]*-boot.jar");

	public static final String BUILD_INFO_VERSION_KEY = "version";
	public static final String BUILD_INFO_TIME_KEY = "build-time";

	private final Path tmpPath;

	private String jarFile;

	private Properties newAppVersion;

	private SortedMap<String, String> migs = new TreeMap<>();
	
	private UpgradeFile upgradeFile;

	public UpgradeUtil(Path zipFile) throws IOException {
		if (zipFile != null) {
			this.tmpPath = extractFolder(zipFile);
			if (jarFile == null) {
				printme("Can't find boot jarFile in " + zipFile.toString());
				return;
			}
			this.setNewAppVersion(findNewAppInfo());
		} else {
			this.tmpPath = null;
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
	
	public boolean writeUpgradeFile(boolean force) throws IOException {
		return writeUpgradeFile(Paths.get(""), force);
	}

	public boolean writeUpgradeFile() throws IOException {
		return writeUpgradeFile(Paths.get(""), false);
	}

	public boolean writeUpgradeFile(Path dir, boolean force) throws IOException {
		return writeUpgradeFile(dir, newAppVersion, force);
	}

	public boolean writeUpgradeFile(Path dir, Properties newAppInfoInfo, boolean force) throws IOException {
		Properties upgradeProperties = new Properties();
		upgradeProperties.setProperty(UpgradeFile.NEW_VESION, newAppInfoInfo.getProperty(BUILD_INFO_VERSION_KEY));
		upgradeProperties.setProperty(UpgradeFile.UPGRADE_JAR, tmpPath.resolve(jarFile).toAbsolutePath().toString());
		String f = tmpPath.toAbsolutePath().toString();
		if (!f.endsWith("\\")) {
			f = f + "\\";
		}
		upgradeProperties.setProperty(UpgradeFile.UPGRADE_FOLDER, f);

		String[] ss = new String[] { APP_VERSION_PROPERTIES_FILE_1, APP_VERSION_PROPERTIES_FILE_2,
				APP_VERSION_PROPERTIES_FILE_3, APP_VERSION_PROPERTIES_FILE_4 };
		InputStream is = null;
		try {
			for (String s : ss) {
				is = ClassLoader.class.getResourceAsStream(s);
				if (is != null)
					break;
			}
			if (is != null) {
				Properties tp = new Properties();
				tp.load(is);
				upgradeProperties.setProperty(UpgradeFile.CURRENT_VESION, tp.getProperty(BUILD_INFO_VERSION_KEY));
			} else {
				throw new UnExpectedInputException(null, "upgrade.cannotdetermineversion", "");
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}

		this.upgradeFile = new UpgradeFile(upgradeProperties);
		if (this.upgradeFile.isUpgradeable() || force) {
			List<String> lines = upgradeProperties.entrySet().stream().map(et -> et.getKey() + "=" + et.getValue())
					.collect(Collectors.toList());
			Files.write(dir.resolve(UPGRADE_FLAG_FILE), lines);
			return true;
		}
		return false;
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

	private Properties findNewAppInfo() throws IOException {
		if (this.tmpPath == null) {
			return null;
		}
		Path jarPath = tmpPath.resolve(jarFile);
		try (JarFile jfile = new JarFile(jarPath.toFile())) {
			JarEntry jentry = jfile.getJarEntry(APP_VERSION_PROPERTIES_FILE_1);
			if (jentry == null) {
				return null;
			}
			try (InputStream is = jfile.getInputStream(jentry)) {
				Properties p = new Properties();
				p.load(is);
				return p;
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

	public Properties getNewAppVersion() {
		return newAppVersion;
	}

	public void setNewAppVersion(Properties newAppVersion) {
		this.newAppVersion = newAppVersion;
	}

	public SortedMap<String, String> getMigs() {
		return migs;
	}

	public void setMigs(SortedMap<String, String> migs) {
		this.migs = migs;
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

	protected static void doChange(Path curPath, Path unZippedPath, Path currentJar, Path newJar, Path dbDir,
			String newVersion) throws IOException {
		backupDb(dbDir);
		backupProperties(curPath, unZippedPath, newVersion);
		backupBat(curPath, unZippedPath);
		backupJar(curPath, currentJar, newJar);
		backupTemplates(curPath, unZippedPath);
	}

	private static void backupJar(Path curPath, Path currentJar, Path newJar) throws IOException {
		Path bak = currentJar.getParent().resolve(currentJar.getFileName().toString() + ".prev");
		Files.move(currentJar, bak);
		Files.copy(newJar, curPath.resolve(newJar.getFileName()));
	}

	private static void backupBat(Path curPath, Path unZippedPath) throws IOException {
		Path curBat = curPath.resolve(CommonFileNames.START_BATCH);
		Path newBat = unZippedPath.resolve(CommonFileNames.START_BATCH);
		FileUtil.backup(curBat, 3, false);
		Files.copy(newBat, curBat);
	}

	private static void backupTemplates(Path curPath, Path unZippedPath) throws IOException {
		Path curTemplatesFolder = curPath.resolve("templates");
		Path newTemplateFolder = unZippedPath.resolve("templates");
		FileUtil.backup(curTemplatesFolder, 3, false);
		FileUtil.copyDirectory(newTemplateFolder, curTemplatesFolder, false);
	}

	private static void backupProperties(Path curPath, Path unZippedPath, String newVersion) throws IOException {
		Path currentApplicationProperties = curPath.resolve(CommonFileNames.APPLICATION_CONFIGURATION);
		Properties pros = new Properties();
		Properties npros = new Properties();
		try (InputStream is = Files.newInputStream(currentApplicationProperties);
				InputStream isn = Files
						.newInputStream(unZippedPath.resolve(CommonFileNames.APPLICATION_CONFIGURATION))) {
			pros.load(is);
			npros.load(isn);
			npros.putAll(pros);
			is.close();
			isn.close();
		}
		FileUtil.backup(currentApplicationProperties, 3, true);
		try (OutputStream os = Files.newOutputStream(currentApplicationProperties)) {
			npros.store(os, newVersion);
			os.close();
		}
	}

	private static void backupDb(Path dbPath) throws IOException {
		FileUtil.backup(dbPath, 3, true);
	}

	public UpgradeFile getUpgradeFile() {
		return upgradeFile;
	}

	public void setUpgradeFile(UpgradeFile upgradeFile) {
		this.upgradeFile = upgradeFile;
	}

}
