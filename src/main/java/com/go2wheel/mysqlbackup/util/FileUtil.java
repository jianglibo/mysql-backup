package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

	public static void deleteFolder(Path folder, Pattern keep) throws IOException {
		deleteFolder(folder, true, keep);
	}
	
	public static void deleteFolder(Path folder, boolean keepRoot) throws IOException {
		deleteFolder(folder, true, null);
	}

	public static void deleteFolder(Path folder, boolean keepRoot, Pattern keep) throws IOException {
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
					if (keep == null || !keep.matcher(file.toString()).matches()) {
						Files.delete(file);
					}
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
	// public static void backup(Path fileOrDirectoryToBackup, int postfixNumber,
	// boolean keepOrigin) throws IOException {
	// int roundNumber = (int) Math.pow(10, postfixNumber) - 1;
	// backup(fileOrDirectoryToBackup, postfixNumber, roundNumber, keepOrigin);
	// }

	public static void backup(Path fileOrDirectoryToBackup, int postfixNumber, int roundNumber, boolean keepOrigin)
			throws IOException {
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

	public static Path joinFile(Path splittedFolder) throws IOException {
		Path meta = splittedFolder.resolve("meta.txt");
		String fn = Files.lines(meta).map(line -> line.split("=")).filter(ss -> ss.length == 2)
				.filter(ss -> ss[0].trim().equals("filename")).findAny().get()[1].trim();
		Path out = splittedFolder.resolve(fn);
		try (OutputStream os = Files.newOutputStream(out)) {
			List<Path> files = Files.list(splittedFolder).filter(p -> p.getFileName().toString().matches("^\\d+$"))
					.collect(Collectors.toList());
			Collections.sort(files);

			byte[] buf = new byte[2048];
			int bytesRead = -1;
			for (Path p : files) {
				try (InputStream is = Files.newInputStream(p)) {
					while ((bytesRead = is.read(buf)) != -1) {
						os.write(buf, 0, bytesRead);
					}
				}
			}

			os.flush();
			os.close();
		}

		return out;
	}

	public static Path splitFile(Path fileToSplit, long maxLength) throws IOException {
		try (InputStream is = Files.newInputStream(fileToSplit)) {
			byte[] buf = new byte[2048];
			int bytesRead = -1;
			long bytesSofar = 0;
			Path splitedFolder = fileToSplit.getParent().resolve(fileToSplit.getFileName().toString() + ".split");
			Files.createDirectories(splitedFolder);
			Path meta = splitedFolder.resolve("meta.txt");
			List<String> lines = new ArrayList<>();
			lines.add("filename=" + fileToSplit.getFileName().toString());
			lines.add("size=" + Files.size(fileToSplit));
			Files.write(meta, lines);
			int fileCount = 0;
			OutputStream writingOs = Files.newOutputStream(splitedFolder.resolve("000"));
			while ((bytesRead = is.read(buf)) != -1) {
				writingOs.write(buf, 0, bytesRead);
				bytesSofar += bytesRead;
				if (bytesSofar > maxLength) {
					writingOs.flush();
					writingOs.close();
					fileCount++;
					String fn = fileCount + "";
					if (fn.length() == 1) {
						fn = "00" + fn;
					} else if (fn.length() == 2) {
						fn = "0" + fn;
					}
					bytesSofar = 0;
					writingOs = Files.newOutputStream(splitedFolder.resolve(fn));
				}
			}

			try {
				writingOs.flush();
				writingOs.close();
				Files.list(splitedFolder).forEach(f -> {
					try {
						if (Files.size(f) == 0L) {
							Files.delete(f);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

			return splitedFolder;
		}
	}

}
