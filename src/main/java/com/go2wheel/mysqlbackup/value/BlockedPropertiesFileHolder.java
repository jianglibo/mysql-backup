package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockedPropertiesFileHolder {
	
	private List<String> lines;
	
	private Pattern blockNamePtn = Pattern.compile(String.format("\\s*\\[%s\\]\\s*", "[^\\[\\]]+"));

	public BlockedPropertiesFileHolder(List<String> lines) {
		super();
		this.lines = lines;
	}
	
	private String getKvLine(ConfigValue cv, Object v) {
		return String.format("%s=%s", cv.getKey(), v);
	}
	
	public void setConfigValue(ConfigValue cv, Object value) {
		switch (cv.getState()) {
		case COMMENT_OUTED:
		case EXIST:
			lines.set(cv.getLineIndex(), getKvLine(cv, value));
			break;
		default:
			if (cv.getBlock() == null || cv.getBlock().trim().isEmpty()) {
				lines.add(cv.getKey() + "=" + value); // put to the last line.
			} else {
				int bp = findBlockPosition(cv.getBlock()); 
				if (bp != -1) {
					lines.set(bp + 1, getKvLine(cv, value));
				} else {
					lines.add(String.format("[%s]", cv.getBlock()));
					lines.add(getKvLine(cv, value));
				}
			}
		}
	}
	
	public int findBlockPosition(String blockName) {
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.indexOf(String.format("[%s]", blockName)) != -1) {
				return i;
			}
		}
		return -1;
	}
	
	public ConfigValue getConfigValue(String blockName, String cnfName) {
		int blkStart = -1, blkEnd = -1;
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.indexOf(String.format("[%s]", blockName)) != -1) {
				blkStart = i+1;
			}
			
			if (blkStart != -1) {
				if (blockNamePtn.matcher(line).matches()) {
					blkEnd = i;
				}
			}
		}
		if (blkEnd == -1) {
			blkEnd = lines.size();
		}
		if (blkStart == -1) {
			return  ConfigValue.getNotExistValue(blockName, cnfName);
		}
		return getConfigValue(blockName, cnfName, blkStart, blkEnd);
	}
	
	private ConfigValue getConfigValue(String block, String cnfName, int blkStart, int blkEnd) {
		Pattern commentOutPtn = getCommentOutedPtn(cnfName);
		Pattern existPtn = getExistedPtn(cnfName);
		ConfigValue commented = ConfigValue.getNotExistValue(block, cnfName);
		Matcher matcher = null;
		for(int i = blkStart; i < blkEnd; i++) {
			String line = lines.get(i);
			matcher = existPtn.matcher(line);
			if (matcher.matches()) {
				return ConfigValue.getExistValue(block, cnfName, matcher.group(1), i);
			}
			
			matcher = commentOutPtn.matcher(line);
			if (matcher.matches()) {
				commented = ConfigValue.getCommentOuted(block, cnfName, matcher.group(1), i);
			}
		}
		return commented;
	}


	public ConfigValue getConfigValue(String cnfName) {
		return getConfigValue(null, cnfName, 0, lines.size());
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
