package com.go2wheel.mysqlbackup;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.jline.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.service.KeyValueDbService;
import com.go2wheel.mysqlbackup.service.KeyValueService;
import com.go2wheel.mysqlbackup.value.KeyValueProperties;
import com.google.common.collect.Sets;

@Component
public class MailPropertiesCustomizer implements EnvironmentAware {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MailProperties mailProperties;

	@Autowired
	private KeyValueService keyValueService;

	@Autowired
	private KeyValueDbService keyValueDbService;
	
	private Environment environment;

	@PostConstruct
	public void after() {
		Set<String> keyNames = Sets.newHashSet("host", "port", "username", "password", "protocol", "jndiName");
		KeyValueProperties mps = keyValueService.getPropertiesByPrefix("spring", "mail");
		keyNames.forEach(kn -> {
			if (!mps.containsKey(kn)) {
				String v = environment.getProperty("spring.mail." + kn);
				if (v != null) {
					KeyValue kv = new KeyValue(new String[] { "spring", "mail", kn}, v);
					keyValueDbService.save(kv);
				}
			}
		});
		Set<String> propertiesKns = mailProperties.getProperties().keySet();
		KeyValueProperties mps1 = keyValueService.getPropertiesByPrefix("spring", "mail", "properties");
		propertiesKns.forEach(kn -> {
			if (!mps1.containsKey(kn)) {
				Map<String, String> pros = mailProperties.getProperties();
				String v = pros.get(kn);
				if (v != null) {
					KeyValue kv = new KeyValue(new String[] { "spring", "mail", "properties", kn}, v);
					keyValueDbService.save(kv);
				}
			}
		});
//		spring.mail.host=smtp.qq.com
//		#465
//		spring.mail.port=587
//		spring.mail.username=jlbfine@qq.com
//		spring.mail.password=emnbsygyqacibgjh
//		spring.mail.protocol=smtp
//		spring.mail.properties.mail.smtp.auth=true
		KeyValueProperties springMail = keyValueService.getPropertiesByPrefix("spring", "mail");
		KeyValueProperties springMailProperties = keyValueService.getPropertiesByPrefix("spring", "mail", "properties");
		mailProperties.setHost(springMail.getProperty("host"));
		mailProperties.setUsername(springMail.getProperty("username"));
		mailProperties.setPassword(springMail.getProperty("password"));
		mailProperties.setProtocol(springMail.getProperty("protocol"));
		mailProperties.getProperties().put("mail.smtp.auth", springMailProperties.getProperty("mail.smtp.auth"));
		Log.info(mailProperties);
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
