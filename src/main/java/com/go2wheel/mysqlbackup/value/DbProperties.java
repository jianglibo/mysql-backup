package com.go2wheel.mysqlbackup.value;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertiesFactoryBean;

public class DbProperties  extends PropertiesFactoryBean {
	
	@Override
	protected Properties createProperties() throws IOException {
		return super.createProperties();
	}

}
