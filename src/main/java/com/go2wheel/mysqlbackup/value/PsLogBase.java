package com.go2wheel.mysqlbackup.value;

import java.util.List;

public class PsLogBase {
	
	private TimeSpan timespan;
	
	private boolean success;
	
	public TimeSpan getTimespan() {
		return timespan;
	}

	public void setTimespan(TimeSpan timespan) {
		this.timespan = timespan;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public static class PsDownloadResult {
		private PsDownloadCatalog total;
		private PsDownloadCatalog failed;
		private PsDownloadCatalog copied;
		
		public PsDownloadCatalog getTotal() {
			return total;
		}
		public void setTotal(PsDownloadCatalog total) {
			this.total = total;
		}
		public PsDownloadCatalog getFailed() {
			return failed;
		}
		public void setFailed(PsDownloadCatalog failed) {
			this.failed = failed;
		}
		public PsDownloadCatalog getCopied() {
			return copied;
		}
		public void setCopied(PsDownloadCatalog copied) {
			this.copied = copied;
		}
	}

	public static class PsDownloadCatalog {
		private List<PsDownloadItem> files;
		private int count;
		private long length;
		public List<PsDownloadItem> getFiles() {
			return files;
		}
		public void setFiles(List<PsDownloadItem> files) {
			this.files = files;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public long getLength() {
			return length;
		}
		public void setLength(long length) {
			this.length = length;
		}
	}
	
	public static class PsDownloadItem {
		private String algorithm;
		private String hash;
		private String path;
		private String localPath;
		private long length;
		public String getAlgorithm() {
			return algorithm;
		}
		public void setAlgorithm(String algorithm) {
			this.algorithm = algorithm;
		}
		public String getHash() {
			return hash;
		}
		public void setHash(String hash) {
			this.hash = hash;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public String getLocalPath() {
			return localPath;
		}
		public void setLocalPath(String localPath) {
			this.localPath = localPath;
		}
		public long getLength() {
			return length;
		}
		public void setLength(long length) {
			this.length = length;
		}
	}

}
