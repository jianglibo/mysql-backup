package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.go2wheel.mysqlbackup.value.CopyEnv;

public class UtilForTe {

	public static void printme(Object o) {
		System.out.println(o);
	}

	public static Path getPathInThisProjectRelative(String fn) {
		Path currentRelativePath = Paths.get("").toAbsolutePath();
		return currentRelativePath.relativize(currentRelativePath.resolve(fn));
	}
	
	public static void deleteFolder(Path folder) throws IOException {
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
	
	
	public static CopyEnv copyEnv() {
		return new CopyEnv(Paths.get(""), Paths.get("..", "abc"), "a.b.c", "c.d.e");
	}
	
	public static CopyEnv copyEnv(String srcRootPackageDot, String dstRootPackageDot) {
		return new CopyEnv(Paths.get(""), Paths.get("..", "abc"), srcRootPackageDot, dstRootPackageDot);
	}
	
	public static CopyEnv copyEnvDemoproject(Path dstFolder, String srcRootPackageDot, String dstRootPackageDot) {
		return new CopyEnv(Paths.get("fixtures", "demoproject"), dstFolder, srcRootPackageDot, dstRootPackageDot);
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
