package com.go2wheel.mysqlbackup;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "copyconfig.disablebuiltins")
@Component
public class DisabledPlugin {
	private Set<String> performers;
	private Set<String> ignorecheckers;
	private Set<String> pathadjusters;

	public Set<String> getPerformers() {
		return performers;
	}

	public void setPerformers(Set<String> performers) {
		this.performers = performers;
	}

	public Set<String> getIgnorecheckers() {
		return ignorecheckers;
	}

	public void setIgnorecheckers(Set<String> ignorecheckers) {
		this.ignorecheckers = ignorecheckers;
	}

	public Set<String> getPathadjusters() {
		return pathadjusters;
	}

	public void setPathadjusters(Set<String> pathadjusters) {
		this.pathadjusters = pathadjusters;
	}

}
