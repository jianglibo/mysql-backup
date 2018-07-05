package com.go2wheel.mysqlbackup.model;

public class KeyValue extends BaseModel {
	
	private String itemKey;
	private String itemValue;
	
	public KeyValue() {}

	public KeyValue(String key, String value) {
		this.setItemKey(key);
		this.setItemValue(value);
	}
	
	public KeyValue(String group, String key, String value) {
		this.setItemKey(group + "." + key);
		this.setItemValue(value);
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


	@Override
	public String toListRepresentation(String... fields) {
		return super.toListRepresentation("itemKey", "itemValue");
	}

}
