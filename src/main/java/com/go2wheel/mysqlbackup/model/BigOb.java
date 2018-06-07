package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class BigOb extends BaseModel {
	
	public static final String SSH_KEYFILE_NAME = "SSH_KEY_FILE";
	public static final String SSH_KNOWN_HOSTS = "SSH_KNOWN_HOSTS";
	
	private String name;
	private String description;
	private byte[] content;

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public byte[] getContent() {
		return content;
	}


	public void setContent(byte[] content) {
		this.content = content;
	}


	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, fields);
	}

}
