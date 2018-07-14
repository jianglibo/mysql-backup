package com.go2wheel.mysqlbackup.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.go2wheel.mysqlbackup.value.CommonMessageKeys;

public class KeyValue extends BaseModel {
	
	@NotEmpty(message=CommonMessageKeys.VALIDATOR_NOTNULL)
	private String itemKey;
	@NotNull(message=CommonMessageKeys.VALIDATOR_NOTNULL)
	private String itemValue;
	
	public KeyValue() {}

	public KeyValue(String key, String value) {
		this.setItemKey(key);
		this.setItemValue(value);
	}

	public KeyValue(String[] keys, String value) {
		String key = String.join(".", keys);
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
