package com.go2wheel.mysqlbackup;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.StandardAPIAutoConfiguration;
import org.springframework.util.StringUtils;

/**
 * Components scan start from this class's package.
 * @author jianglibo@gmail.com
 *
 */

// StandardAPIAutoConfiguration

@SpringBootApplication(exclude= {SpringShellAutoConfiguration.class, JLineShellAutoConfiguration.class, StandardAPIAutoConfiguration.class})
public class StartPointer {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
        String[] disabledCommands = {"--spring.shell.command.stacktrace.enabled=false"}; 
        String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);
        
		ConfigurableApplicationContext context = SpringApplication.run(StartPointer.class, fullArgs);
	}
	
	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString("my-shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}
}
