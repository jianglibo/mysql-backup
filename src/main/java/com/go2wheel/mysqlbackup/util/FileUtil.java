package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

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
	// public static void moveOrCopyFileOrDirectory(boolean keepOrigin, PathPair
	// pair) throws IOException {
	// Files.copy(pair.getSrc(), pair.getDst(), StandardCopyOption.COPY_ATTRIBUTES);
	// if (!keepOrigin) {
	// deleteFolder(pair.getSrc());
	// }
	// }

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

	public static void copyDirectory(Path srcDirectory, Path dstDirectory, boolean removeExists) throws IOException {

		Path srcDirectoryAbs = srcDirectory.toAbsolutePath();
		Path dstDirectoryAbs = dstDirectory.toAbsolutePath();

		if (Files.exists(dstDirectoryAbs) && Files.list(dstDirectoryAbs).count() > 0) {
			if (removeExists) {
				deleteFolder(dstDirectoryAbs, true);
			} else {
				throw new FileAlreadyExistsException(dstDirectoryAbs.toString());
			}
		}
		Files.walkFileTree(srcDirectoryAbs, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				file = file.toAbsolutePath();
				Path fileRelative = srcDirectoryAbs.relativize(file);
				Path dst = dstDirectoryAbs.resolve(fileRelative);
				Path pdst = dst.getParent();
				if (Files.exists(pdst)) {

				} else if (Files.notExists(pdst)) {

				} else {
					logger.error("cannot determine file exists, {}", pdst.toString());
				}
				Files.createDirectories(pdst);
				Files.copy(file, dst);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void deleteFolder(Path folder, boolean keepRoot) throws IOException {
		if (folder == null || !Files.exists(folder)) {
			return;
		}
		Path folderFinal = folder.toAbsolutePath();
		if (Files.isRegularFile(folderFinal)) {
			Files.delete(folderFinal);
		} else {
			Files.walkFileTree(folderFinal, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (keepRoot) {
						if (!dir.equals(folderFinal)) {
							Files.delete(dir);
						}
					} else {
						Files.delete(dir);
					}
					if (exc != null)
						throw exc;
					return FileVisitResult.CONTINUE;
				}
			});
		}

	}

	// /**
	// * Use spring core util instead.
	// *
	// * @param folders
	// * @throws IOException
	// */
	// public static void deleteFolder(Path... folders) throws IOException {
	// for (Path folder : folders) {
	// if (folder == null || !Files.exists(folder)) {
	// return;
	// }
	// if (Files.isRegularFile(folder)) {
	// Files.delete(folder);
	// } else {
	// FileSystemUtils.deleteRecursively(folder);
	// }
	// }
	// }

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
		int roundNumber = (int) Math.pow(10, postfixNumber);
		backup(fileOrDirectoryToBackup, postfixNumber, roundNumber, keepOrigin);
	}
	
	public static void backup(Path fileOrDirectoryToBackup, int postfixNumber, int roundNumber, boolean keepOrigin) throws IOException {
		if (!Files.exists(fileOrDirectoryToBackup)) {
			logger.error("Source file: '{}' does't exists.", fileOrDirectoryToBackup.toAbsolutePath().toString());
			return;
		}
		Path target = PathUtil.getNextAvailable(fileOrDirectoryToBackup, postfixNumber, roundNumber);
		if (Files.isDirectory(fileOrDirectoryToBackup)) {
			copyDirectory(fileOrDirectoryToBackup, target, true);
			if (!keepOrigin) {
				deleteFolder(fileOrDirectoryToBackup, false);
			}
		} else {
			Files.copy(fileOrDirectoryToBackup, target, StandardCopyOption.COPY_ATTRIBUTES,
					StandardCopyOption.REPLACE_EXISTING);
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

	// nioBasicFileAttributes(path);

	public static void nioBasicFileAttributes(Path path) throws IOException {

		BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);

		// Print basic file attributes
		System.out.println("Creation Time: " + basicFileAttributes.creationTime());
		System.out.println("Last Access Time: " + basicFileAttributes.lastAccessTime());
		System.out.println("Last Modified Time: " + basicFileAttributes.lastModifiedTime());
		System.out.println("Size: " + basicFileAttributes.size());
		System.out.println("Is Regular file: " + basicFileAttributes.isRegularFile());
		System.out.println("Is Directory: " + basicFileAttributes.isDirectory());
		System.out.println("Is Symbolic Link: " + basicFileAttributes.isSymbolicLink());
		System.out.println("Other: " + basicFileAttributes.isOther());

		// modify the lastmodifiedtime
		FileTime newModifiedTime = FileTime.fromMillis(basicFileAttributes.lastModifiedTime().toMillis() + 60000);
		Files.setLastModifiedTime(path, newModifiedTime);
		// check if the lastmodifiedtime is changed
		System.out.println("After Changing lastModifiedTime, ");
		System.out.println("Creation Time: " + basicFileAttributes.creationTime());
		System.out.println("Last Access Time: " + basicFileAttributes.lastAccessTime());
		System.out.println("Last Moodified Time: " + basicFileAttributes.lastModifiedTime());
	}
}
