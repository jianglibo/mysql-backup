package com.go2wheel.mysqlbackup.model;

import java.util.Date;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class KeyValueInDb extends BaseModel {
	
	public static final String OBNAME_MYSQL = "mysql";
	
//	CONSTRAINT unique_kv_idnamekey UNIQUE (object_id, object_name, the_key)
	
	private Integer objectId;
	private String objectName;
	private String theKey;
	private String theValue;

	public Integer getObjectId() {
		return objectId;
	}

	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getTheKey() {
		return theKey;
	}

	public void setTheKey(String theKey) {
		this.theKey = theKey;
	}

	public String getTheValue() {
		return theValue;
	}

	public void setTheValue(String theValue) {
		this.theValue = theValue;
	}

	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, "objectId", "objectName", "theKey", "theValue");
	}
	
	public static KeyValueInDb newMysqlKv(int obid, String key, String value) {
		KeyValueInDb kv = new KeyValueInDb();
		kv.setCreatedAt(new Date());
		kv.setObjectId(obid);
		kv.setObjectName(OBNAME_MYSQL);
		kv.setTheKey(key);
		kv.setTheValue(value);
		return kv;
	}

}
