package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.standard.StandardAPIAutoConfiguration;
import org.springframework.util.StringUtils;

import com.go2wheel.mysqlbackup.util.FileUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil.UpgradeFile;

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
		doUpgrade(Paths.get(""), args);
		String[] disabledCommands = { "--spring.shell.command.quit.enabled=false" };
		// String[] disabledCommands =
		// {"--spring.shell.command.stacktrace.enabled=false"};
		String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);

		// ConfigurableApplicationContext context =
		// SpringApplication.run(StartPointer.class, fullArgs);
		ConfigurableApplicationContext context = new SpringApplicationBuilder(StartPointer.class)
				.listeners(new ApplicationPidFileWriter("./bin/app.pid")).logStartupInfo(false).run(fullArgs);
	}

	protected static void doUpgrade(Path curPath, String[] args) throws IOException {
		// --spring.datasource.url=jdbc:hsqldb:file:%wdirslash%%_db%;shutdown=true

		if (!Files.exists(Paths.get(UpgradeUtil.UPGRADE_FLAG_FILE))) {
			logger.info("no upgrade file {} found. skiping.",
					Paths.get(UpgradeUtil.UPGRADE_FLAG_FILE).toAbsolutePath().toString());
			return;
		}
		logger.info("start upgrading...");
		Pattern ptn = Pattern.compile(".*jdbc:hsqldb:file:([^;]+);.*");
		String dbPath = null;
		Optional<Path> currentJarOp = Files.list(curPath)
				.filter(p -> UpgradeUtil.JAR_FILE_PTN.matcher(p.getFileName().toString()).matches()).findAny();
		if (!currentJarOp.isPresent()) {
			logger.error("Cannot locate current jar file.");
			return;
		}
		Path currentJar = currentJarOp.get();
		
		for (String s : args) {
			Matcher m = ptn.matcher(s);
			if (m.matches()) {
				dbPath = m.group(1);
			}
		}

		// This pattern is fixed.
		logger.info("db path: {}", dbPath);
		if (dbPath != null) {
			String newDbdir = dbPath.replaceAll("/db", "");
			FileUtil.backup(3, false, Paths.get(newDbdir));
			String origindbDir = dbPath.replaceAll(".prev/db", "");
			Files.copy(Paths.get(origindbDir), Paths.get(newDbdir), StandardCopyOption.COPY_ATTRIBUTES);
			logger.info("copy db folder, from {} to {}", origindbDir, newDbdir);
		}

		String propfn = "application.properties";
		String startBatFn = "start.bat";


		Path currentApplicationProperties = Paths.get(propfn);
		try {
			UpgradeFile uf = new UpgradeFile(Paths.get(UpgradeUtil.UPGRADE_FLAG_FILE));
			Path newJar = Paths.get(uf.getUpgradeJar());
			Path newBat = newJar.getParent().resolve(startBatFn);
			FileUtil.backup(3, true, Paths.get(startBatFn));
			Files.copy(newBat, Paths.get(startBatFn));

			Properties pros = new Properties();
			Properties npros = new Properties();
			try (InputStream is = Files.newInputStream(currentApplicationProperties);
					InputStream isn = Files.newInputStream(newJar.getParent().resolve(propfn))) {
				pros.load(is);
				npros.load(isn);
				npros.putAll(pros);
			}
			
			FileUtil.backup(3, true, currentApplicationProperties);

			try (OutputStream os = Files.newOutputStream(currentApplicationProperties)) {
				npros.store(os, uf.getNewVersion());
			}
			Path bak = currentJar.getParent().resolve(currentJar.getFileName().toString() + ".prev");
			Files.move(currentJar, bak);
			Files.copy(newJar, curPath.resolve(newJar.getFileName()));
		} catch (IOException e) {
			e.printStackTrace();
		}

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

}
