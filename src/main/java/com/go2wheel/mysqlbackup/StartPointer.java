package com.go2wheel.mysqlbackup;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.standard.StandardAPIAutoConfiguration;
import org.springframework.util.StringUtils;

/**
 * Components scan start from this class's package.
 * 
 * @author jianglibo@gmail.com
 *
 */

// StandardAPIAutoConfiguration

@SpringBootApplication(exclude = { SpringShellAutoConfiguration.class, JLineShellAutoConfiguration.class,
		StandardAPIAutoConfiguration.class })
@EnableAspectJAutoProxy
public class StartPointer {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	// This line of code will cause flyway initializing earlier.
	@SuppressWarnings("unused")
	@Autowired
	private FlywayMigrationInitializer flywayInitializer;

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		String[] disabledCommands = {};
		// String[] disabledCommands =
		// {"--spring.shell.command.stacktrace.enabled=false"};
		String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);

		// ConfigurableApplicationContext context =
		// SpringApplication.run(StartPointer.class, fullArgs);
		ConfigurableApplicationContext context = new SpringApplicationBuilder(StartPointer.class).listeners(new ApplicationPidFileWriter("./bin/app.pid")).logStartupInfo(false)
				.run(fullArgs);
	}

	
	@Bean
	@ConfigurationProperties(prefix = "spring.messages")
	public MessageSourceProperties messageSourceProperties() {
		return new MessageSourceProperties();
	}

	@Bean
	public MessageSource messageSource() {
		MessageSourceProperties properties = messageSourceProperties();
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		String bn = properties.getBasename();
		if (StringUtils.hasText(bn)) {
			messageSource.setBasenames(StringUtils.commaDelimitedListToStringArray(
					StringUtils.trimAllWhitespace(properties.getBasename())));
		}
		if (properties.getEncoding() != null) {
			messageSource.setDefaultEncoding(properties.getEncoding().name());
		}
		messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
		Duration cacheDuration = properties.getCacheDuration();
		if (cacheDuration != null) {
			messageSource.setCacheMillis(cacheDuration.toMillis());
		}
		messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
		messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
		return messageSource;
	}

}
