package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class RemoteCommandResult {
	
	private String stdOut;
	
	private String errOut;
	
	private Optional<String> reason = Optional.empty();
	
	private int exitValue;
	
	private String command;
	
	public RemoteCommandResult() {}
	
	public RemoteCommandResult(String stdOut, String errOut, int exitValue) {
		this.stdOut = stdOut;
		this.errOut = errOut;
		this.exitValue = exitValue;
	}
	
	public static RemoteCommandResult partlyResult(String stdOut, int exitValue) {
		RemoteCommandResult er = new RemoteCommandResult();
		er.setStdOut(stdOut);
		er.setExitValue(exitValue);
		return er;
	}
	
	public static RemoteCommandResult failedResult(String reason) {
		RemoteCommandResult er = new RemoteCommandResult();
		er.reason = Optional.of(reason);
		return er;
	}
	
	public boolean isExitValueNotEqZero() {
		return getExitValue() != 0;
	}
	
	public boolean isCommandNotFound() {
		return getAllTrimedNotEmptyLines().stream().anyMatch(line -> line.contains("command not found"));
	}
	
	public List<String> getAllTrimedNotEmptyLines() {
		return Stream.of(getStdOut(), getErrOut()).flatMap(str -> StringUtil.splitLines(str).stream()).filter(line -> StringUtil.hasAnyNonBlankWord(line)).collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return String.format("[exitValue: %s, reason: %s]", getExitValue(), getReason());
	}

	public String getStdOut() {
		if (stdOut == null) return "";
		return stdOut;
	}

	public void setStdOut(String stdOut) {
		this.stdOut = stdOut;
	}

	public String getErrOut() {
		if (stdOut == null) return "";
		return errOut;
	}

	public void setErrOut(String errOut) {
		this.errOut = errOut;
	}

	public void setReason(Optional<String> reason) {
		this.reason = reason;
	}

	public Optional<String> getReason() {
		return reason;
	}

	public int getExitValue() {
		return exitValue;
	}

	public void setExitValue(int exitValue) {
		this.exitValue = exitValue;
	}
	
	public void printOutput() {
		for(String line : getAllTrimedNotEmptyLines()) {
			System.out.println(line);
		}
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
}
