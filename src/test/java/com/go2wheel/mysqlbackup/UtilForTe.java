package com.go2wheel.mysqlbackup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.go2wheel.mysqlbackup.MyAppSettings.SshConfig;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class UtilForTe {
	
	private static Pattern getItemPtn(String name) {
		return Pattern.compile("\\s+" + name + ":\\s*(.*?)\\s*");
	}

	public static void printme(Object o) {
		System.out.println(o);
	}
	
	public static MyAppSettings getMyAppSettings() {
		InputStream is =ClassLoader.class.getResourceAsStream("/application.yml");
		
		 MyAppSettings mas = new MyAppSettings();
		 SshConfig sc = new SshConfig();
		 mas.setSsh(sc);
		if (is != null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			try {
				while((line = in.readLine()) != null) {
					Matcher m = getItemPtn("sshIdrsa").matcher(line);
					if (m.matches()) {
						 sc.setSshIdrsa((String) m.group(1));
					}
					m = getItemPtn("knownHosts").matcher(line);
					if (m.matches()) {
						sc.setKnownHosts((m.group(1)));
					}
				}
			} catch (IOException e) {
			}
			 
		}
		return mas;
	}

	public static YmlConfigFort getYmlConfigFort() {
		InputStream is =ClassLoader.class.getResourceAsStream("/test.yml"); 
		if (is != null) {
			return YamlInstance.INSTANCE.getYaml().loadAs(is, YmlConfigFort.class);
		} else {
			return new YmlConfigFort();
		}
		
	}
	
	
	public static BackupCommand backupCommandInstance() throws IOException {
		BackupCommand bc = new BackupCommand();
		bc.setInstancesBase(Files.createTempDirectory("backupcommandbase"));
		return bc;
	}
	
	
	public static Path getMysqlInstanceDescription(String hostname) {
		return Paths.get("fixtures", "mysqls", hostname, "description.yml");
	}
	
	public static MysqlInstance getDemoInstance() {
		return getYmlConfigFort().getDemoinstance();
	}

	public static Path getPathInThisProjectRelative(String fn) {
		Path currentRelativePath = Paths.get("").toAbsolutePath();
		return currentRelativePath.relativize(currentRelativePath.resolve(fn));
	}
	
	public static void deleteFolder(Path folder) throws IOException {
		if (folder == null || !Files.exists(folder)) {
			return;
		}
		
		if (Files.isRegularFile(folder)) {
			Files.delete(folder);
		}
		Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
		    @Override
		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		        throws IOException
		    {
		    	Files.delete(file);
		        return FileVisitResult.CONTINUE;
		    }
	    @Override
	    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
	        throws IOException
	    {
	    	Files.delete(dir);
	        if (exc != null)
	            throw exc;
	        return FileVisitResult.CONTINUE;
	    }
		});
	}
	
	public static Path createTmpDirectory() throws IOException {
		return Files.createTempDirectory("tmpdirforpc");
	}
	
	public static Path createFileTree(String...fns) throws IOException {
		Path tmpFolder = Files.createTempDirectory("tmpfiletrees");
		for(String fn : fns) {
			String sanitized = fn.trim().replace('\\', '/');
			if (sanitized.startsWith("/")) {
				sanitized = sanitized.substring(1);
			}
			Path p = Paths.get(sanitized);
			String fileName = p.getFileName().toString();
			Path parent = p.getParent();
			Path brandNewDirectory;
			if (parent != null) {
				brandNewDirectory = Files.createDirectories(tmpFolder.resolve(parent));
			} else {
				brandNewDirectory = tmpFolder;
			}
			if (fileName.indexOf('.') != -1) { // it's a file.
				Files.write(brandNewDirectory.resolve(fileName), "hello".getBytes());
			} else {
				Files.createDirectories(brandNewDirectory.resolve(fileName));
			}
		}
		return tmpFolder;
	}

}
