package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.aop.TimeCost;

public class ProcessExecResult implements TimeCost {

	private int exitValue;

	private List<String> stdOut;

	private List<String> stdError;

	private Exception exception;

	private long startTime;

	private long endTime;

	public ProcessExecResult() {
	}

	public ProcessExecResult(int exitValue) {
		this.exitValue = exitValue;
	}

	public int getExitValue() {
		return exitValue;
	}

	public void setExitValue(int exitValue) {
		this.exitValue = exitValue;
	}

	public List<String> getStdOut() {
		return stdOut;
	}

	public List<String> getStdOutFilterEmpty() {
		return getStdOut().stream().map(l -> l.trim()).filter(l -> !l.isEmpty()).collect(Collectors.toList());
	}

	public void setStdOut(List<String> stdOut) {
		this.stdOut = stdOut;
	}

	public List<String> getStdError() {
		return stdError;
	}

	public void setStdError(List<String> stdError) {
		this.stdError = stdError;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
