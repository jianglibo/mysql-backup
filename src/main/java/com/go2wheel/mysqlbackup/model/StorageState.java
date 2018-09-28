package com.go2wheel.mysqlbackup.model;

import java.util.Comparator;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class StorageState extends BaseModel {
	
	
	public static Comparator<StorageState> AVAILABLE_DESC = new Comparator<StorageState>() {

		@Override
		public int compare(StorageState o1, StorageState o2) {
			return (int) (o2.getAvailable() - o1.getAvailable());
		}
	};
	
	private Integer serverId;
	private String root;
	private long used;
	private long available;
	
	public double getUsedRatio() {
		double d = (double)used / (used + available) * 100; 
		return d;
	}
	
	public double getUsedRatioTrue() {
		double d = (double)used / (used + available); 
		return d;
	}

	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public long getUsed() {
		return used;
	}
	public void setUsed(long used) {
		this.used = used;
	}

	public long getAvailable() {
		return available;
	}

	public void setAvailable(long available) {
		this.available = available;
	}

	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, fields);
	}

}
