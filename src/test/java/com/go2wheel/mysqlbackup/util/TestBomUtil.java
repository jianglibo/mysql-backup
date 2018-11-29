package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class TestBomUtil {
	
	private Path boma = Paths.get("fixtures", "boma.txt");
	
	@Test
	public void t() throws IOException {
		byte[] bytes = Files.readAllBytes(boma);
		
		assertTrue(bytes[0] == BomUtil.EF);
		assertTrue(bytes[1] == BomUtil.BB);
		assertTrue(bytes[2] == BomUtil.BF);
		assertTrue(bytes[3] == 'a');
		
		assertThat(bytes[0], equalTo(BomUtil.EF));
		assertThat(bytes[1], equalTo(BomUtil.BB));
		assertThat(bytes[2], equalTo(BomUtil.BF));
		assertThat(bytes[3], equalTo((byte)'a'));
		
		bytes = BomUtil.removeBom(bytes).getBytes();
		
		assertThat(bytes.length, equalTo(1));
		assertTrue(bytes[0] == 'a');
	}

}
