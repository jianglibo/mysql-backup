package com.go2wheel.mysqlbackup.model;

public class KeyKeyValue extends BaseModel {
	
//	 CONSTRAINT unique_key_key_value UNIQUE (group_key, item_key, item_value)
	private String groupKey;
	private String itemKey;
	private String itemValue;
	
	public String getGroupKey() {
		return groupKey;
	}
	public void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}
	public String getItemKey() {
		return itemKey;
	}
	public void setItemKey(String itemKey) {
		this.itemKey = itemKey;
	}
	public String getItemValue() {
		return itemValue;
	}
	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}


}
