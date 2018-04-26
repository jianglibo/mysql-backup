package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.go2wheel.mysqlbackup.value.ConfigValue.ConfigValueState;

public class MycnfFileHolder extends BlockedPropertiesFileHolder {
	
	public static final String LOG_BIN = "log_bin";
//	Holds the base name and path for the binary log files, which can be set with the --log-bin server option. In MySQL 5.7, the default base name is the name of the host machine with the suffix -bin. The default location is the data directory.
	public static final String LOG_BIN_BASENAME = "log_bin_basename";
	public static final String LOG_BIN_INDEX = "log_bin_index";
	

	public MycnfFileHolder(List<String> lines) {
		super(lines);
	}
	
	/**
	 * 
	 * @return true if changed or else false.
	 */
	public boolean enableBinLog(String...filename) {
		Optional<String> fnOp = filename.length > 0 ? Optional.of(filename[0]) : Optional.empty(); 
		ConfigValue cv = getConfigValue(LOG_BIN);
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
					getLines().add(i + 1, LOG_BIN + "=" + fnOp.orElse("mysql-bin"));
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
