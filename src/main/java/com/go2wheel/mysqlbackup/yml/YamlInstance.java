package com.go2wheel.mysqlbackup.yml;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

public enum YamlInstance {
	INSTANCE;
	
	public final Yaml yaml;
	private YamlInstance() {
		Representer representer = new Representer();
		representer.getPropertyUtils().setSkipMissingProperties(true);
		this.yaml = new Yaml(representer);
	}
}
