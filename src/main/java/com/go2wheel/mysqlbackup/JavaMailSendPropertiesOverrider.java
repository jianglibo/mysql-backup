package com.go2wheel.mysqlbackup;

import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.dbservice.KeyValueDbService;
import com.go2wheel.mysqlbackup.dbservice.KeyValueService;
import com.go2wheel.mysqlbackup.event.ModelAfterCreatedEvent;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.value.KeyValueProperties;
import com.google.common.collect.Sets;

// @Component
public class JavaMailSendPropertiesOverrider implements EnvironmentAware {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String SPRING_MAIL_USERNAME= "spring.mail.username";
	public static final String SPRING_MAIL_PORT= "spring.mail.port";
	public static final String SPRING_MAIL_HOST= "spring.mail.host";
	public static final String SPRING_MAIL_PASSWORD= "spring.mail.password";
	public static final String SPRING_MAIL_PROTOCOL= "spring.mail.protocol";
	public static final String SPRING_MAIL_PROPERTIES_SMTP_AUTH= "spring.mail.properties.mail.smtp.auth"; // mail.smtp.auth -> JavaMailSenderImpl mailproperites.

	@Autowired
	private MailProperties mailProperties;

	@Autowired
	private KeyValueService keyValueService;

	@Autowired
	private KeyValueDbService keyValueDbService;
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	private Environment environment;

	@PostConstruct
	public void after() {
		createDbMailSettingsIfNotExists();
		copyFromDbToMailSender();
	}



	private void copyFromDbToMailSender() {
		KeyValueProperties mailPropertiesIndb = keyValueService.getPropertiesByPrefix("spring", "mail");
		JavaMailSenderImpl jmsi = (JavaMailSenderImpl) javaMailSender;
		jmsi.setHost(mailPropertiesIndb.getProperty("host"));
		jmsi.setPassword(mailPropertiesIndb.getProperty("password"));
		jmsi.setPort(mailPropertiesIndb.getInteger("port"));
		jmsi.setProtocol(mailPropertiesIndb.getProperty("protocol"));
		jmsi.setUsername(mailPropertiesIndb.getProperty("username"));
		
		mailProperties.setHost(jmsi.getHost());
		mailProperties.setUsername(jmsi.getUsername());
		mailProperties.setPassword(jmsi.getPassword());
		mailProperties.setProtocol(jmsi.getProtocol());
	}


	private void createDbMailSettingsIfNotExists() {
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
	}


	@EventListener
	public void whenKvChanged(ModelChangedEvent<KeyValue> keyvalueChangedEvent) throws SchedulerException, ParseException {
		KeyValue kv = keyvalueChangedEvent.getAfter();
		if (kv.getItemKey().startsWith("spring.mail.")) {
			copyFromDbToMailSender();
		}
	}

	@EventListener
	public void whenKeyValueCreated(ModelAfterCreatedEvent<KeyValue> keyvalueCreatedEvent) throws SchedulerException, ParseException {
		KeyValue kv = keyvalueCreatedEvent.getModel(); 
		if (kv.getItemKey().startsWith("spring.mail.")) {
			copyFromDbToMailSender();
		}
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
	
//	spring.mail.host=smtp.qq.com
//	#465
//	spring.mail.port=587
//	spring.mail.username=jlbfine@qq.com
//	spring.mail.password=emnbsygyqacibgjh
//	spring.mail.protocol=smtp
//	spring.mail.properties.mail.smtp.auth=true

//	Log.info(mailProperties);
}
