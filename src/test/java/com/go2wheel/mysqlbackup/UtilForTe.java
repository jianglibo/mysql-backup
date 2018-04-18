package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class UtilForTe {

	public static void printme(Object o) {
		System.out.println(o);
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
