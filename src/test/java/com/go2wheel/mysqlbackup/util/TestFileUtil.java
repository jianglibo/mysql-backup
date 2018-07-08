package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

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
	public void tBackupDirModoru() throws IOException {
		prepareDirs();
		Path folder = dir.resolve("a.b");
		Files.createDirectories(folder);
		Files.write(folder.resolve("afile.txt"), "abc".getBytes());
		for(int i = 0; i < 11; i++) { //loop 11 times, so there should be 11 file under dir. when loop to 10, it will return to 0;
			FileUtil.backup(folder, 1, true);
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
			FileUtil.backup(file, 1, true);
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

		FileUtil.backup(dir, 3, true);
		FileUtil.backup(dir1, 3, true);

		FileUtil.backup(dir, 3, false);
		FileUtil.backup(dir1, 3, false);

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

		FileUtil.backup(dir, 1, false);
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
