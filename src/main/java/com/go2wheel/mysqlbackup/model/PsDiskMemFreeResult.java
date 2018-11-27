package com.go2wheel.mysqlbackup.model;

import java.util.List;

public class PsDiskMemFreeResult extends PsLogBase {
	
	private List<PsDiskMemFreeItem> result;
	
	public static class PsDiskMemFreeItem {
        private long used;
        private long free;
        private String percent;
        private String freem;
        private String name;
        private String usedm;
		public long getUsed() {
			return used;
		}
		public void setUsed(long used) {
			this.used = used;
		}
		public long getFree() {
			return free;
		}
		public void setFree(long free) {
			this.free = free;
		}
		public String getPercent() {
			return percent;
		}
		public void setPercent(String percent) {
			this.percent = percent;
		}
		public String getFreem() {
			return freem;
		}
		public void setFreem(String freem) {
			this.freem = freem;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getUsedm() {
			return usedm;
		}
		public void setUsedm(String usedm) {
			this.usedm = usedm;
		}
	}


	public List<PsDiskMemFreeItem> getResult() {
		return result;
	}


	public void setResult(List<PsDiskMemFreeItem> result) {
		this.result = result;
	}

}
