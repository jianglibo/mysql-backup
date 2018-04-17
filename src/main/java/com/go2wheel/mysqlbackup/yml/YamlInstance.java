package com.go2wheel.mysqlbackup.yml;

import org.yaml.snakeyaml.Yaml;

public enum YamlInstance {
	INSTANCE;
	
	private final Yaml yaml;
	private YamlInstance() {
		this.yaml = new Yaml();
	}
	public Yaml getYaml() {
		return yaml;
	}
}