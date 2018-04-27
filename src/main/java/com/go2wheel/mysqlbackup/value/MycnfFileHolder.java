package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.go2wheel.mysqlbackup.value.ConfigValue.ConfigValueState;

public class MycnfFileHolder extends BlockedPropertiesFileHolder {
	
	public static final String DEFAULT_LOG_BIN_BASE_NAME = "hm-log-bin";

	public MycnfFileHolder(List<String> lines) {
		super(lines);
	}
	
	public boolean enableBinLog() {
		return enableBinLog(DEFAULT_LOG_BIN_BASE_NAME);
	}
	
	public byte[] toByteArray() {
		return String.join("\n", getLines()).getBytes();
	}
	
	/**
	 * 
	 * @return true if changed or else false.
	 */
	public boolean enableBinLog(String filename) {
		Optional<String> fnOp = filename == null || filename.trim().isEmpty() ? Optional.empty() : Optional.of(filename.trim()); 
		ConfigValue cv = getConfigValue(LogBinSetting.LOG_BIN);
		if (cv.getState() == ConfigValueState.EXIST) {
			if (fnOp.isPresent()) {
				if (cv.getValue().equals(fnOp.get())) {
					return false;
				}
			}
		}
		boolean changed = false;
		switch (cv.getState()) {
		case EXIST: // set new value.
		case COMMENT_OUTED:
			if (fnOp.isPresent()) {
				getLines().set(cv.getLineIndex(), cv.getKey() + "=" + fnOp.get());
			} else {
				getLines().set(cv.getLineIndex(), cv.getKey() + "=" + cv.getValue());
			}
			changed = true;
			break;
		case NOT_EXIST:
			Pattern ptn = Pattern.compile("\\s*\\[mysqld\\]\\s*");
			for(int i=0; i< getLines().size(); i++) {
				String line = getLines().get(i);
				if (ptn.matcher(line).matches()) {
					getLines().add(i + 1, LogBinSetting.LOG_BIN + "=" + fnOp.orElse(DEFAULT_LOG_BIN_BASE_NAME));
					changed = true;
				}
			}
			break;
		default:
			break;
		}
		return changed;
	}
	
}
