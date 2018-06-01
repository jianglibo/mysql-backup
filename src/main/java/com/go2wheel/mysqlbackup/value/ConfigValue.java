package com.go2wheel.mysqlbackup.value;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class ConfigValue {
	
	public static enum ConfigValueState {
		EXIST, COMMENT_OUTED, NOT_EXIST
	}
	
	private ConfigValueState state;
	private String value;
	private String key;
	private String block;
	private int lineIndex;
	
	public static ConfigValue getExistValue(String blockName, String key, String value, int lineIndex) {
		ConfigValue cv = new ConfigValue();
		cv.state = ConfigValueState.EXIST;
		cv.value = value;
		cv.key = key;
		cv.setBlock(blockName);
		cv.setLineIndex(lineIndex);
		return cv;
	}
	
	public static ConfigValue getCommentOuted(String blockName, String key, String value, int lineIndex) {
		ConfigValue cv = new ConfigValue();
		cv.state = ConfigValueState.COMMENT_OUTED;
		cv.value = value;
		cv.key = key;
		cv.setBlock(blockName);
		cv.setLineIndex(lineIndex);
		return cv;
	}
	
	public static ConfigValue getNotExistValue(String blockName, String key) {
		ConfigValue cv = new ConfigValue();
		cv.block = blockName;
		cv.state = ConfigValueState.NOT_EXIST;
		cv.key = key;
		return cv;
	}
	
	@Override
	public String toString() {
		return ObjectUtil.dumpObjectAsMap(this);
	}
	
	private ConfigValue() {
	}

	public ConfigValueState getState() {
		return state;
	}

	public void setState(ConfigValueState state) {
		this.state = state;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getLineIndex() {
		return lineIndex;
	}

	public void setLineIndex(int lineIndex) {
		this.lineIndex = lineIndex;
	}

	public String getBlock() {
		return block;
	}

	public void setBlock(String block) {
		this.block = block;
	}
}
