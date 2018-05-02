package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.go2wheel.mysqlbackup.exception.AtomicWriteFileException;
import com.go2wheel.mysqlbackup.exception.LocalBackupFileException;
import com.go2wheel.mysqlbackup.exception.LocalFileMoveException;

public class FileUtil {

	public static void moveFilesAllOrNone(Path[]... pairs) throws LocalFileMoveException {
		try {
			for (Path[] pair : pairs) {
				Files.move(pair[0], pair[1], StandardCopyOption.ATOMIC_MOVE);
			}
		} catch (IOException e) {
			List<Path> unrecoverFiles = new ArrayList<>();
			for (Path[] pair : pairs) {
				if (Files.exists(pair[1])) {
					if (!Files.exists(pair[0])) {
						try {
							Files.move(pair[1], pair[0], StandardCopyOption.ATOMIC_MOVE);
						} catch (IOException e1) {
							unrecoverFiles.add(pair[0]);
						}
					} else {
						// will not happen.
					}
				}
			}
			if (unrecoverFiles.size() > 0) {
				throw new LocalFileMoveException(unrecoverFiles);
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
			}
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

	public static void createNewBackupAndRemoveOrigin(int postfixNumber, Path... files) throws LocalBackupFileException, LocalFileMoveException {
		int len = files.length;
		Path[][] pairs = new Path[len][];

		for (int i = 0; i < len; i++) {
			Path file = files[i];
			if (!Files.exists(file)) {
				throw new LocalBackupFileException(String.format("Source file: '%s' does't exists.", file.toString()));
			}
			Path file1 = PathUtil.getNextAvailable(file, postfixNumber);
			if (Files.exists(file1)) {
				throw new LocalBackupFileException(String.format("Destnation file: '%s' exists.", file1.toString()));
			}
			pairs[i] = new Path[] {file, file1};
		}
		moveFilesAllOrNone(pairs);
	}
	
	public static void atomicWriteFile(Path dstFile, byte[] content) throws AtomicWriteFileException {
		try {
			String fn = dstFile.getFileName().toString() + ".writing";
			Path tmpFile = dstFile.getParent().resolve(fn);
			Files.write(tmpFile, content);
			Files.move(tmpFile, dstFile, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			throw new AtomicWriteFileException(dstFile);
		}
	}
}
