package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.go2wheel.mysqlbackup.value.ConfigValue.ConfigValueState;

public class MyCnfHolder {
	
	private List<String> lines;

	public MyCnfHolder(List<String> lines) {
		super();
		this.lines = lines;
	}
	
	/**
	 * 
	 * @return true if changed or else false.
	 */
	public boolean enableBinLog(String...filename) {
		Optional<String> fnOp = filename.length > 0 ? Optional.of(filename[0]) : Optional.empty(); 
		ConfigValue cv = getConfigValue("log-bin");
		if (cv.getState() == ConfigValueState.EXIST) {
			if (fnOp.isPresent()) {
				if (cv.getValue().equals(fnOp.get())) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public ConfigValue getConfigValue(String cnfName) {
		Pattern commentOutPtn = getCommentOutedPtn(cnfName);
		Pattern existPtn = getExistedPtn(cnfName);
		ConfigValue commented = ConfigValue.getNotExistValue(cnfName);
		Matcher matcher = null;
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			matcher = existPtn.matcher(line);
			if (matcher.matches()) {
				return ConfigValue.getExistValue(cnfName, matcher.group(1), i);
			}
			
			matcher = commentOutPtn.matcher(line);
			if (matcher.matches()) {
				commented = ConfigValue.getCommentOuted(cnfName, matcher.group(1), i);
			}
		}
		return commented;
	}
	
	private Pattern getCommentOutedPtn(String nameOfItem) {
		return Pattern.compile("^\\s*;\\s*" + nameOfItem + "\\s*=\\s*([^\\s]+)\\s*$");
	}
	
	private Pattern getExistedPtn(String nameOfItem) {
		return Pattern.compile("^\\s*" + nameOfItem + "\\s*=\\s*([^\\s]+)\\s*$");
	}

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
	}
}
