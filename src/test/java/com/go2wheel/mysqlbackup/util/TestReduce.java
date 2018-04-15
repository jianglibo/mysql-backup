package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.value.CopyDescription;
import com.go2wheel.mysqlbackup.value.CopyDescription.COPY_STATE;
import com.go2wheel.mysqlbackup.value.CopyResult;

public class TestReduce {
	

	@Test
	public void tCollect() {
		long start = System.currentTimeMillis();
		int count = 10000;
		CopyResult ttcr = IntStream.range(0, count).mapToObj(i -> {
			if (i % 2 == 0) {
				CopyDescription cd = new CopyDescription(Paths.get("srcFolder"), Paths.get("1")); 
				cd.setState(COPY_STATE.FILE_COPY_SUCCESSED);
				return cd;
			} else {
				CopyDescription cd = new CopyDescription(Paths.get("srcFolder"), Paths.get("2"));
				cd.setState(COPY_STATE.FILE_COPY_FAILED);
				return cd;
			}
			}).parallel()
				.collect(CopyResult::new, CopyResult::accept, CopyResult::combine);
		UtilForTe.printme(System.currentTimeMillis() - start);
		assertThat(ttcr.getDescriptionMap().get(COPY_STATE.FILE_COPY_SUCCESSED).size(), equalTo(0));
		assertThat(ttcr.getCountMap().get(COPY_STATE.FILE_COPY_SUCCESSED), equalTo(5000L));

		ttcr = IntStream.range(0, count).mapToObj(i -> {
			if (i % 2 == 0) {
				CopyDescription cd = new CopyDescription(Paths.get("srcFolder"), Paths.get("1")); 
				cd.setState(COPY_STATE.FILE_COPY_SUCCESSED);
				return cd;
			} else {
				CopyDescription cd = new CopyDescription(Paths.get("srcFolder"), Paths.get("2"));
				cd.setState(COPY_STATE.FILE_COPY_FAILED);
				return cd;
			}
			}).collect(CopyResult::new,
				CopyResult::accept, CopyResult::combine);
		UtilForTe.printme(System.currentTimeMillis() - start);
		assertThat(ttcr.getDescriptionMap().get(COPY_STATE.FILE_COPY_SUCCESSED).size(), equalTo(0));
		assertThat(ttcr.getCountMap().get(COPY_STATE.FILE_COPY_SUCCESSED), equalTo(5000L));
	}
}
