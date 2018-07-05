package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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

	/**
	 * Files.copy can copy files and directory (only directory, no including files
	 * in.).
	 * 
	 * @param keepOrigin
	 * @param filePairs
	 * @throws IOException
	 */
//	public static void moveOrCopyFileOrDirectory(boolean keepOrigin, PathPair pair) throws IOException {
//		Files.copy(pair.getSrc(), pair.getDst(), StandardCopyOption.COPY_ATTRIBUTES);
//		if (!keepOrigin) {
//			deleteFolder(pair.getSrc());
//		}
//	}

	public static class PathPair {
		private Path src;
		private Path dst;

		public PathPair(Path src, Path dst) {
			super();
			this.src = src;
			this.dst = dst;
		}

		public Path getSrc() {
			return src;
		}

		public void setSrc(Path src) {
			this.src = src;
		}

		public Path getDst() {
			return dst;
		}

		public void setDst(Path dst) {
			this.dst = dst;
		}

	}

	public static void copyDirectory(Path srcDirectory, Path dstDirectory) throws IOException {
		if (Files.exists(dstDirectory) && Files.list(dstDirectory).count() > 0) {
			throw new FileAlreadyExistsException(dstDirectory.toAbsolutePath().toString());
		}
		Files.walkFileTree(srcDirectory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				file = file.toAbsolutePath();
				Path fileRelative = srcDirectory.relativize(file);
				Path dst = dstDirectory.resolve(fileRelative);
				if (!Files.exists(dst.getParent())) {
					Files.createDirectories(dst.getParent());
				}
				Files.copy(file, dst);
				return FileVisitResult.CONTINUE;
			}
		});
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

	/**
	 * backup create a new file|directory as origin's sibling with name appended an
	 * increasing number. It's not recursive.
	 * 
	 * @param postfixNumber
	 * @param keepOrigin
	 * @param fileOrDirectoryToBackup
	 * @throws IOException
	 */
	public static void backup(Path fileOrDirectoryToBackup, int postfixNumber, boolean keepOrigin) throws IOException {
		if (!Files.exists(fileOrDirectoryToBackup)) {
			logger.error("Source file: '{}' does't exists.", fileOrDirectoryToBackup.toAbsolutePath().toString());
			return;
		}
		Path target = PathUtil.getNextAvailable(fileOrDirectoryToBackup, postfixNumber);
		if (Files.exists(target)) {
			logger.error("Destnation file: '{}' already exists.", target.toAbsolutePath().toString());
		}
		if (Files.isDirectory(fileOrDirectoryToBackup)) {
			Files.createDirectories(target);
			copyDirectory(fileOrDirectoryToBackup, target);
			if (!keepOrigin) {
				deleteFolder(fileOrDirectoryToBackup);
			}
		} else {
			Files.copy(fileOrDirectoryToBackup, target, StandardCopyOption.COPY_ATTRIBUTES);
			if (!keepOrigin) {
				Files.delete(fileOrDirectoryToBackup);
			}
		}
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
