package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;

public class TestCopyEnv {
	
	@Test
	public void t() {
		CopyEnv copyEnv = UtilForTe.copyEnv("a.b.c", "c.d");
		assertThat(copyEnv.getEscapedSrcRootPackageDot(), equalTo("a\\.b\\.c"));
		
		assertThat(copyEnv.getSrcRootPackageSlash(), equalTo("a/b/c"));
		assertThat(copyEnv.getDstRootPackageSlash(), equalTo("c/d"));
	}

}
