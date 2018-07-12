package com.go2wheel.mysqlbackup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.service.JobLogDbService;


@Controller
@RequestMapping(JobLogsController.MAPPING_PATH)
public class JobLogsController  extends CRUDController<JobLog, JobLogDbService> {
	
	public static final String MAPPING_PATH = "/app/job-logs";
	
	@Autowired
	public JobLogsController(JobLogDbService dbService) {
		super(JobLog.class, dbService, MAPPING_PATH);
	}

	@Override
	boolean copyProperties(JobLog entityFromForm, JobLog entityFromDb) {
		return false;
	}

	@Override
	public JobLog newModel() {
		return new JobLog();
	}

	@Override
	protected void formAttribute(Model model) {
	}

	@Override
	protected void listExtraAttributes(Model model) {
	}
	
	@Override
	protected int getMenuOrder() {
		return 500;
	}
}
