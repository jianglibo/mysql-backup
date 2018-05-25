package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryNotEmptyException;
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
			FileUtil.deleteFolder(dir, dir1);
		} catch (Exception e) {
		}
	}
	
	@Test
	public void tSuccess() throws IOException {
		Files.write(dir.resolve("a.b.0"), "abc".getBytes());
		Path dst = dir.getParent().resolve(dir.getFileName().toString() + ".1");
		
		Files.write(dir1.resolve("a.b.0"), "abc".getBytes());
		Path dst1 = dir1.getParent().resolve(dir1.getFileName().toString() + ".1");

		FileUtil.moveFilesAllOrNone(false, new Path[] {dir, dst}, new Path[] {dir1, dst1});
		
		assertTrue(Files.exists(dst));
		assertFalse(Files.exists(dir));
		
		assertTrue(Files.exists(dst1));
		assertFalse(Files.exists(dir1));
	}
	
	@Test
	public void tmove() throws IOException {
		Files.write(dir.resolve("a.b.0"), "abc".getBytes());
		Path dst000 = dir.getParent().resolve(dir.getFileName().toString() + ".000");
		Path dst001 = dir.getParent().resolve(dir.getFileName().toString() + ".001");
		
		Files.write(dir1.resolve("a.b.0"), "abc".getBytes());
		Path dst1_000 = dir1.getParent().resolve(dir1.getFileName().toString() + ".000");
		Path dst1_001 = dir1.getParent().resolve(dir1.getFileName().toString() + ".001");
		
		FileUtil.backup(3,false, dir, dir1);
		
		Files.createDirectories(dir);
		Files.createDirectories(dir1);
		
		FileUtil.backup(3,false, dir, dir1);
		
		assertTrue(Files.exists(dst000));
		assertTrue(Files.exists(dst001));
		assertFalse(Files.exists(dir));
		
		assertTrue(Files.exists(dst1_000));
		assertTrue(Files.exists(dst1_001));
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
	
	@Test(expected=DirectoryNotEmptyException.class)
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

		FileUtil.moveFilesAllOrNone(false, new Path[] {dir, dst}, new Path[] {dir1, dst1});
		
		assertTrue(Files.exists(dir));
		assertFalse(Files.exists(dst));
		
		assertTrue(Files.exists(dir1));
		assertFalse(Files.exists(dst1));

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
