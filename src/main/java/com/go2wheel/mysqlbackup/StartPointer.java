package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil;
import com.go2wheel.mysqlbackup.value.DbProperties;

/**
 * Components scan start from this class's package.
 * 
 * @author jianglibo@gmail.com
 *
 */

// StandardAPIAutoConfiguration

@SpringBootApplication(exclude = {})
//@SpringBootApplication(exclude = { SpringShellAutoConfiguration.class,
//		JLineShellAutoConfiguration.class,
//		StandardAPIAutoConfiguration.class})
@EnableAspectJAutoProxy
@EnableAsync
public class StartPointer {

	private static Logger logger = LoggerFactory.getLogger(StartPointer.class);

	// This line of code will cause flyway initializing earlier.
	@SuppressWarnings("unused")
	@Autowired
	private FlywayMigrationInitializer flywayInitializer;

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		logger.info("---start args----");
		for (String a : args) {
			logger.info(a);
		}
		logger.info("---start args----");
		UpgradeUtil.doUpgrade(Paths.get(""), args);
		String[] disabledCommands = { "--spring.shell.command.quit.enabled=false" };
		// String[] disabledCommands =
		// {"--spring.shell.command.stacktrace.enabled=false"};
		String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);

		// ConfigurableApplicationContext context =
		// SpringApplication.run(StartPointer.class, fullArgs);
		ConfigurableApplicationContext context = new SpringApplicationBuilder(StartPointer.class)
				.listeners(new ApplicationPidFileWriter("./bin/app.pid"), new ApplicationListener<ApplicationReadyEvent>() {
					@Override
					public void onApplicationEvent(ApplicationReadyEvent event) {
				    	Path upgrade = Paths.get(UpgradeUtil.UPGRADE_FLAG_FILE);
				    	if (Files.exists(upgrade)) {
				    		try {
								Files.delete(upgrade);
								System.exit(BackupCommand.RESTART_CODE);
							} catch (IOException e) {
								ExceptionUtil.logErrorException(logger, e);
							}
				    	}
					}
				}).logStartupInfo(false).run(fullArgs);
	}
	


	@Bean
	@ConfigurationProperties(prefix = "spring.messages")
	public MessageSourceProperties messageSourceProperties() {
		return new MessageSourceProperties();
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource ds) {
		return new JdbcTemplate(ds);
	}
	
    @Bean()
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder build) {
    	build.featuresToDisable(SerializationFeature.INDENT_OUTPUT);
    	ObjectMapper mapper = build.build();
    	return mapper;
    }
    

	@Bean
	public MessageSource messageSource() {
		MessageSourceProperties properties = messageSourceProperties();
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		String bn = properties.getBasename();
		if (StringUtils.hasText(bn)) {
			messageSource.setBasenames(StringUtils
					.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(properties.getBasename())));
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
	
	
    @Bean("propertiesInDb")
    public DbProperties dbProperties() {
        DbProperties factory = new DbProperties();
        return factory;
    }
}
