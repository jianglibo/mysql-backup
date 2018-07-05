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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFileUtil {

	private Path dir;
	private Path dir1;

	@Before
	public void before() throws IOException {
		dir = Files.createTempDirectory("fileutil");
		dir1 = Files.createTempDirectory("fileutil");
	}

	@After
	public void after() throws IOException {
		try {
			Path tmp = dir.getParent();
			Files.list(tmp).filter(p -> Files.isDirectory(p)).filter(p -> {
				String fn = p.getFileName().toString(); 
				return fn.startsWith("fileutil");
			}).forEach(p -> {
				try {
					FileUtil.deleteFolder(p);
				} catch (IOException e) {
				}
			});
			FileUtil.deleteFolder(dir, dir1);
		} catch (Exception e) {
		}
	}

	@Test(expected = FileAlreadyExistsException.class)
	public void copyExists() throws IOException {
		Files.copy(dir, dir1);
		assertThat(Files.list(dir).count(), equalTo(0L));

		Files.write(dir.resolve("a"), "abc".getBytes());

		assertThat(Files.list(dir).count(), equalTo(1L));

	}

	@Test
	public void tCopyDirectoryNoTarget() throws IOException {
		String fn = "a.b.0";
		Files.write(dir.resolve(fn), "abc".getBytes());
		Path dd = dir.resolve("dd");
		Files.createDirectories(dd);
		Files.write(dd.resolve(fn), "abc".getBytes());

		FileUtil.copyDirectory(dir, dir1);

		assertTrue("level one file should copied.", Files.exists(dir1.resolve(fn)));
		assertTrue("level two file should copied.", Files.exists(dir1.resolve("dd").resolve(fn)));

	}

	@Test
	public void tmove() throws IOException {
		Files.write(dir.resolve("a.b.0"), "abc".getBytes());
		Path dst000 = dir.getParent().resolve(dir.getFileName().toString() + ".000");
		Path dst001 = dir.getParent().resolve(dir.getFileName().toString() + ".001");

		Files.write(dir1.resolve("a.b.0"), "abc".getBytes());
		Path dst1000 = dir1.getParent().resolve(dir1.getFileName().toString() + ".000");
		Path dst1001 = dir1.getParent().resolve(dir1.getFileName().toString() + ".001");

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
	public void t() {
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
	public void tFailed() throws IOException {
		Path f = dir.resolve("a.b.0");
		Files.write(f, "abc".getBytes());
		Path dst = dir.getParent().resolve(dir.getFileName().toString() + ".1");

		Files.write(dir1.resolve("a.b.0"), "abc".getBytes());
		Path dst1 = dir1.getParent().resolve(dir1.getFileName().toString() + ".1");

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

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		FileUtil.backup(dir, 1, false);
	}

	@Test
	public void tmoveToExists() throws IOException {
		Path f1 = Files.createTempFile("fileutil", ".txt");
		Path f2 = Files.createTempFile("fileutil", ".txt");
		Files.write(f1, "abc".getBytes());
		Files.write(f2, "abc".getBytes());
		Files.move(f1, f2, StandardCopyOption.ATOMIC_MOVE);

		assertFalse(Files.exists(f1));
		assertTrue(Files.exists(f2));
	}
}
