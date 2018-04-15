package com.go2wheel.mysqlbackup.value;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

import com.go2wheel.mysqlbackup.exception.TargetOverlapSourceException;

public class CopyEnv {
	
	private Path srcFolder;
	private Path dstFolder;
	
	private String srcRootPackageDot;
	private String dstRootPackageDot;
	
	private String srcRootPackageSlash;
	private String dstRootPackageSlash;

	private String escapedSrcRootPackageDot;
	
	private Path ignoreFile;
	
	private List<PathMatcher> pathMatchers;

	public CopyEnv(Path srcFolder, Path dstFolder, String srcRootPackageDot, String dstRootPackageDot) {
		super();
		if (dstFolder.startsWith(srcFolder) || dstFolder.equals(srcFolder)) {
			throw new TargetOverlapSourceException(srcFolder.toString(), dstFolder.toString());
		}
		this.srcFolder = srcFolder.normalize();
		this.dstFolder = dstFolder.normalize();
		this.srcRootPackageDot = srcRootPackageDot;
		this.dstRootPackageDot = dstRootPackageDot;
		this.escapedSrcRootPackageDot = srcRootPackageDot.replaceAll("\\.", "\\\\.");
		this.srcRootPackageSlash = this.srcRootPackageDot.replace('.', '/');
		this.dstRootPackageSlash = this.dstRootPackageDot.replace('.', '/');
	}
	

	public String getSrcRootPackageSlash() {
		return srcRootPackageSlash;
	}



	public void setSrcRootPackageSlash(String srcRootPackageSlash) {
		this.srcRootPackageSlash = srcRootPackageSlash;
	}



	public String getDstRootPackageSlash() {
		return dstRootPackageSlash;
	}



	public void setDstRootPackageSlash(String dstRootPackageSlash) {
		this.dstRootPackageSlash = dstRootPackageSlash;
	}



	public String getSrcRootPackageDot() {
		return srcRootPackageDot;
	}



	public void setSrcRootPackageDot(String srcRootPackageDot) {
		this.srcRootPackageDot = srcRootPackageDot;
	}



	public String getDstRootPackageDot() {
		return dstRootPackageDot;
	}



	public void setDstRootPackageDot(String dstRootPackageDot) {
		this.dstRootPackageDot = dstRootPackageDot;
	}



	public Path getIgnoreFile() {
		return ignoreFile;
	}

	public Path getSrcFolder() {
		return srcFolder;
	}
	public void setSrcFolder(Path srcFolder) {
		this.srcFolder = srcFolder;
	}
	public Path getDstFolder() {
		return dstFolder;
	}
	public void setDstFolder(Path dstFolder) {
		this.dstFolder = dstFolder;
	}

	public void setIgnoreFile(Path ignoreFile) {
		this.ignoreFile = ignoreFile;
	}

	public List<PathMatcher> getPathMatchers() {
		return pathMatchers;
	}

	public void setPathMatchers(List<PathMatcher> pathMatchers) {
		this.pathMatchers = pathMatchers;
	}



	public String getEscapedSrcRootPackageDot() {
		return escapedSrcRootPackageDot;
	}



	public void setEscapedSrcRootPackageDot(String escapedSrcRootPackageDot) {
		this.escapedSrcRootPackageDot = escapedSrcRootPackageDot;
	}


	public Path getDstRelative(CopyDescription cd) {
		return dstFolder.relativize(cd.getDstAb());
	}
}
