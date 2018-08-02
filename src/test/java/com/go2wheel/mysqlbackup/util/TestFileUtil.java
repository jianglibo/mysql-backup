package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestFileUtil {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();

	private Path dir;
	private Path dir1;
	
	private void prepareDirs() throws IOException {
		dir = tfolder.newFolder().toPath();
		dir1 = tfolder.newFolder().toPath();
	}
	
	@Test
	public void testSort() {
		List<String> ls = new ArrayList<>();
		ls.add("001");
		ls.add("099");
		ls.add("000");
		ls.add("1000");
		Collections.sort(ls);
		
		assertThat(ls, contains("000", "001", "099", "1000"));
	}
	
//	@Test
//	public void testBigFile() throws IOException {
//		Path f = Paths.get("D:", "webpic.rar");
//		Path splittedFolder = FileUtil.splitFile(f, 1024 * 1024 * 256);
//		FileUtil.joinFile(splittedFolder);
//	}
	
	@Test
	public void testSplitByteFile() throws IOException {
		Path fileToSplit = tfolder.newFile().toPath();
		byte[] bs = "你说了算！".getBytes(StandardCharsets.UTF_8);
		assertThat(bs.length, equalTo(15));
		try (OutputStream os = Files.newOutputStream(fileToSplit)) {
			// Writing 10000 times, the result file is 150 x 15 == 2250 in length. 
			IntStream.range(0, 150).forEach(i -> {
				try {
					os.write(bs);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		long originLength = Files.size(fileToSplit);
		assertThat(originLength, equalTo(2250L));
		
		Path splitedFolder = FileUtil.splitFile(fileToSplit, 2049);
		
		long num = Files.list(splitedFolder).count();
		assertThat(num, equalTo(2L)); // includes meta file.
		
		long totalBytes = Files.list(splitedFolder).mapToLong(f -> {
			try {
				if (f.getFileName().toString().equals("meta.txt")) {
					return 0L;
				}
				return Files.size(f);
			} catch (IOException e) {
				return 0L;
			}
		}).sum();
		// the total bytes after splitting should be same to origin file.
		assertThat(totalBytes, equalTo(originLength));
		
		Path joined = FileUtil.joinFile(splitedFolder);
		
		assertThat(Files.size(joined), equalTo(originLength));
	}
	

	@Test
	public void tBackupDirModoru() throws IOException {
		prepareDirs();
		Path folder = dir.resolve("a.b");
		Files.createDirectories(folder);
		Files.write(folder.resolve("afile.txt"), "abc".getBytes());
		for(int i = 0; i < 15; i++) { //loop 11 times, so there should be 11 file under dir. when loop to 10, it will return to 0;
			FileUtil.backup(folder, 1, 9, true);
		}
		assertThat(Files.list(dir).count(), equalTo(11L));
	}

	
	@Test
	public void tCreateAndDeleteFolderTmpFile() throws IOException {
		Path subfolder = tfolder.newFolder("kkk").toPath();
		Files.createDirectories(subfolder);
		Files.write(subfolder.resolve("kkk"), "abc".getBytes());
		FileUtil.deleteFolder(subfolder, false);
		Files.createDirectories(subfolder);
	}


	
	@Test
	public void tBackupFileModoru() throws IOException {
		prepareDirs();
		Path file = dir.resolve("a.b");
		Files.write(file, "abc".getBytes());
		for(int i = 0; i < 11; i++) { //loop 11 times, so there should be 11 file under dir. when loop to 10, it will return to 0;
			FileUtil.backup(file, 1,9, true);
		}
		assertThat(Files.list(dir).count(), equalTo(11L));
	}

	@Test(expected = FileAlreadyExistsException.class)
	public void copyExists() throws IOException {
		prepareDirs();
		Files.copy(dir, dir1);
		assertThat(Files.list(dir).count(), equalTo(0L));
		Files.write(dir.resolve("a"), "abc".getBytes());
		assertThat(Files.list(dir).count(), equalTo(1L));
	}

	@Test
	public void tCopyDirectoryToEmptyExistsDir() throws IOException {
		prepareDirs();
		String fn = "a.b.0.txt";
		Path fp = dir.resolve(fn);
		Files.write(fp, "abc".getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		Path dd = dir.resolve("dd");
		Files.createDirectories(dd);
		Files.write(dd.resolve(fn), "abc".getBytes());
		
		// dir1 exists and is empty.
		FileUtil.copyDirectory(dir, dir1, false);

		assertTrue("level one file should copied.", Files.exists(dir1.resolve(fn)));
		assertTrue("level two file should copied.", Files.exists(dir1.resolve("dd").resolve(fn)));

	}
	
	@Test
	public void tCopyDirectoryToExistsDirAndNotEmpty() throws IOException {
		prepareDirs();
		String fn = "a.b.0.txt";
		Path fp = dir.resolve(fn);
		Files.write(fp, "abc".getBytes());
		Path dd = dir.resolve("dd");
		Files.createDirectories(dd);
		Files.write(dd.resolve(fn), "abc".getBytes());
		
		Files.write(dir1.resolve(fn), "abc".getBytes());
		// dir1 exists and is empty.
		FileUtil.copyDirectory(dir, dir1, true);

		assertTrue("level one file should copied.", Files.exists(dir1.resolve(fn)));
		assertTrue("level two file should copied.", Files.exists(dir1.resolve("dd").resolve(fn)));

	}


	@Test
	public void tBackupTwice() throws IOException {
		prepareDirs();
		Files.write(dir.resolve("a.b.0"), "abc".getBytes());
		String dirName = dir.getFileName().toString();
		Path dst000 = dir.getParent().resolve(dirName + ".000");
		Path dst001 = dir.getParent().resolve(dirName + ".001");

		Files.write(dir1.resolve("a.b.0"), "abc".getBytes());
		String dir1Name = dir1.getFileName().toString();
		Path dst1000 = dir1.getParent().resolve(dir1Name + ".000");
		Path dst1001 = dir1.getParent().resolve(dir1Name + ".001");

		FileUtil.backup(dir, 3, 999, true);
		FileUtil.backup(dir1, 3, 999, true);

		FileUtil.backup(dir, 3, 999, false);
		FileUtil.backup(dir1, 3, 999, false);

		assertTrue(Files.exists(dst000));
		assertTrue(Files.exists(dst001));
		assertFalse(Files.exists(dir));

		assertTrue(Files.exists(dst1000));
		assertTrue(Files.exists(dst1001));
		assertFalse(Files.exists(dir1));

	}

	@Test
	public void tArrayIndexOutOfBoundsException() {
		try {
			int[][] ii = new int[2][];
			ii[2][0] = 0;
		} catch (Exception e) {
			assertTrue(e.getMessage().endsWith("2"));
		}

		try {
			int[][] ii = new int[2][];
			ii[1] = new int[1];
			ii[1][1] = 0;
		} catch (Exception e) {
			assertTrue(e.getMessage().endsWith("1"));
		}

	}

	@Test(expected = DirectoryNotEmptyException.class)
	public void tUnreleasedInputstreamWillCauseCopyFailed() throws IOException {
		prepareDirs();
		Path f = dir.resolve("a.b.0");
		Files.write(f, "abc".getBytes());
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InputStream is = Files.newInputStream(f);
					Thread.sleep(500);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		FileUtil.backup(dir, 1, 9, false);
	}
	


	@Test
	public void tMoveToExists() throws IOException {
		prepareDirs();
		Path f1 = Files.write(dir.resolve("1.txt"), "abc".getBytes());
		Path f2 = Files.write(dir.resolve("2.txt"), "abc".getBytes());
		Files.write(f1, "abc".getBytes());
		Files.write(f2, "abc".getBytes());
		Files.move(f1, f2, StandardCopyOption.ATOMIC_MOVE);

		assertFalse(Files.exists(f1));
		assertTrue(Files.exists(f2));
	}
}
