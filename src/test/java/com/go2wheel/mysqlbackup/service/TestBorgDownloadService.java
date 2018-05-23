package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.job.BorgArchiveJob;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.value.ResultEnum;

public class TestBorgDownloadService extends ServiceTbase {

	@Autowired
	private BorgArchiveJob borgArchiveJob;

	@Test
	public void t() throws JobExecutionException {
		borgArchiveJob.execute(context);

		List<BorgDownload> downloads = borgDownloadService.getItemsInDays(box, 3);

		assertThat(downloads.size(), equalTo(1));
		
		assertThat(downloads.get(0).getResult(), equalTo(ResultEnum.SUCCESS));

	}

}
