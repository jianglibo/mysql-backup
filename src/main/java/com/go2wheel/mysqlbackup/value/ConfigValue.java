package com.go2wheel.mysqlbackup.value;

public class ConfigValue {
	
	public static enum ConfigValueState {
		EXIST, COMMENT_OUTED, NOT_EXIST
	}
	
	private ConfigValueState state;
	private String value;
	private String key;
	private int lineIndex;
	
	public static ConfigValue getExistValue(String key, String value, int lineIndex) {
		ConfigValue cv = new ConfigValue();
		cv.state = ConfigValueState.EXIST;
		cv.value = value;
		cv.key = key;
		cv.setLineIndex(lineIndex);
		return cv;
	}
	
	public static ConfigValue getCommentOuted(String key, String value, int lineIndex) {
		ConfigValue cv = new ConfigValue();
		cv.state = ConfigValueState.COMMENT_OUTED;
		cv.value = value;
		cv.key = key;
		cv.setLineIndex(lineIndex);
		return cv;
	}
	
	public static ConfigValue getNotExistValue(String key) {
		ConfigValue cv = new ConfigValue();
		cv.state = ConfigValueState.NOT_EXIST;
		cv.key = key;
		return cv;
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
}
