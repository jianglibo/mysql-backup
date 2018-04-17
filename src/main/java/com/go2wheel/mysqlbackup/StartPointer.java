package com.go2wheel.mysqlbackup;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
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
        String[] disabledCommands = {}; 
//        String[] disabledCommands = {"--spring.shell.command.stacktrace.enabled=false"}; 
        String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);
        
//		ConfigurableApplicationContext context = SpringApplication.run(StartPointer.class, fullArgs);
        ConfigurableApplicationContext context = new SpringApplicationBuilder(StartPointer.class).logStartupInfo(false).run(fullArgs);
	}
	

}
