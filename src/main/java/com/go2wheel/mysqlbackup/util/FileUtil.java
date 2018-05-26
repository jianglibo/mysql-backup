package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

	public static void moveFilesAllOrNone(boolean keepOrigin, Path[]... pairs) throws IOException  {
		for (Path[] pair : pairs) {
			Files.copy(pair[0], pair[1], StandardCopyOption.COPY_ATTRIBUTES);
			if (!keepOrigin) {
				 deleteFolder(pair[0]);
			}
		}
	}

	public static void deleteFolder(Path... folders) throws IOException {
		for (Path folder : folders) {
			if (folder == null || !Files.exists(folder)) {
				return;
			}

			if (Files.isRegularFile(folder)) {
				Files.delete(folder);
			} else {
				Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						if (exc != null)
							throw exc;
						return FileVisitResult.CONTINUE;
					}
				});
			}
		}
	}

	public static void backup(int postfixNumber,boolean keepOrigin, Path... files) throws IOException {
		int len = files.length;
		Path[][] pairs = new Path[len][];
		int idx = 0;

		for (int i = 0; i < len; i++) {
			Path file = files[i];
			if (!Files.exists(file)) {
				logger.error("Source file: '{}' does't exists.", file.toAbsolutePath().toString());
				continue;
			}
			Path file1 = PathUtil.getNextAvailable(file, postfixNumber);
			if (Files.exists(file1)) {
				logger.error("Destnation file: '{}' does't exists.", file1.toAbsolutePath().toString());
				continue;
			}
			pairs[idx] = new Path[] {file, file1};
			idx++;
		}
		moveFilesAllOrNone(keepOrigin, pairs);
	}
	
	public static void atomicWriteFile(Path dstFile, byte[] content) throws IOException {
		String fn = dstFile.getFileName().toString() + ".writing";
		Path tmpFile = dstFile.getParent().resolve(fn);
		Files.write(tmpFile, content);
		try {
			Files.move(tmpFile, dstFile, StandardCopyOption.ATOMIC_MOVE);
		} catch (Exception e) {
			Files.write(dstFile, content);
		}
	}
}
