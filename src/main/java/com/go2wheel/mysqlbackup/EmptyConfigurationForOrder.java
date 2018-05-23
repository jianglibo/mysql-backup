package com.go2wheel.mysqlbackup;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({FlywayAutoConfiguration.class, FlywayConfiguration.class})
@AutoConfigureBefore({QuartzAutoConfiguration.class})
public class EmptyConfigurationForOrder {

}
