package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockedPropertiesFileHolder {
	
	private List<String> lines;

	public BlockedPropertiesFileHolder(List<String> lines) {
		super();
		this.lines = lines;
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
