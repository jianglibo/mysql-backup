package com.go2wheel.mysqlbackup.cfgoverrides.jlineshellautoconfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.shell.CommandRegistry;
import org.springframework.shell.MethodTargetRegistrar;
import org.springframework.shell.ParameterResolver;
import org.springframework.shell.standard.CommandValueProvider;
import org.springframework.shell.standard.EnumValueProvider;
import org.springframework.shell.standard.StandardAPIAutoConfiguration;
import org.springframework.shell.standard.StandardMethodTargetRegistrar;
import org.springframework.shell.standard.StandardParameterResolver;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.valueprovider.BorgDescriptionProvider;
import com.go2wheel.mysqlbackup.valueprovider.BoxDescriptionProvider;
import com.go2wheel.mysqlbackup.valueprovider.BoxTriggerProvider;
import com.go2wheel.mysqlbackup.valueprovider.CronStringValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.DefaultValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.FileValueProviderMine;
import com.go2wheel.mysqlbackup.valueprovider.MysqlDescriptionProvider;
import com.go2wheel.mysqlbackup.valueprovider.PossibleValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.ServerGrpValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.ServerInfoProvider;
import com.go2wheel.mysqlbackup.valueprovider.ServerValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.UserAccountValueProvider;

/**
 * copy from {@link StandardAPIAutoConfiguration}
 * @author jianglibo@gmail.com
 *
 */

@Configuration
public class StandardAPIAutoConfigurationMine {

	@Bean
	public ValueProvider commandValueProvider(@Lazy CommandRegistry commandRegistry) {
		return new CommandValueProvider(commandRegistry);
	}

	@Bean
	public ValueProvider enumValueProvider() {
		return new EnumValueProvider();
	}
	
	@Bean
	public ValueProvider hostValueProvider() {
		return new ServerInfoProvider();
	}

	@Bean
	public ValueProvider serverValueProvider() {
		return new ServerValueProvider();
	}
	
	@Bean
	public ValueProvider cronStringValueProvider() {
		return new CronStringValueProvider();
	}

	@Bean
	public ValueProvider serverGrpValueProvider() {
		return new ServerGrpValueProvider();
	}

	@Bean
	public ValueProvider userAccountValueProvider() {
		return new UserAccountValueProvider();
	}


	@Bean
	public ValueProvider fileValueProvider() {
		return new FileValueProviderMine();
	}

	@Bean
	public ValueProvider mysqlDescriptionProvider() {
		return new MysqlDescriptionProvider();
	}
	
	@Bean
	public ValueProvider boxDescriptionProvider() {
		return new BoxDescriptionProvider();
	}

	@Bean
	public ValueProvider defaultValueProvider() {
		return new DefaultValueProvider();
	}
	
	@Bean
	public ValueProvider possibleValueProvider() {
		return new PossibleValueProvider();
	}
	

	
	@Bean
	public ValueProvider borgDescriptionProvider() {
		return new BorgDescriptionProvider();
	}

	@Bean
	public ValueProvider boxTriggerProvider() {
		return new BoxTriggerProvider();
	}

	@Bean
	public MethodTargetRegistrar standardMethodTargetResolver() {
		return new StandardMethodTargetRegistrar();
	}

	@Bean
	public ParameterResolver standardParameterResolver(@Qualifier("spring-shell") ConversionService conversionService) {
		return new StandardParameterResolver(conversionService);
	}
}
