package com.go2wheel.mysqlbackup.model;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class JobLog extends BaseModel {
	
	private String jobClass;
	private String ctx;
	private String exp;
	
	public JobLog() {}
	public JobLog(Class<? extends Job> jobClass, JobExecutionContext context, String throwable) {
		this.jobClass = jobClass.getName();
		this.ctx = context.toString();
		this.exp = throwable;
	}
	
	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public String getCtx() {
		return ctx;
	}

	public void setCtx(String ctx) {
		this.ctx = ctx;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, fields);
	}

}
