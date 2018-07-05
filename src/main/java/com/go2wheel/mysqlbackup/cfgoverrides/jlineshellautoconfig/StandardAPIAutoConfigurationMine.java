package com.go2wheel.mysqlbackup.cfgoverrides.jlineshellautoconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.standard.StandardAPIAutoConfiguration;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.valueprovider.CronStringValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.DbTableNameProvider;
import com.go2wheel.mysqlbackup.valueprovider.DefaultValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.FileValueProviderMine;
import com.go2wheel.mysqlbackup.valueprovider.JobKeyProvider;
import com.go2wheel.mysqlbackup.valueprovider.KeyValueValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.ObjectFieldValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.OstypeProvider;
import com.go2wheel.mysqlbackup.valueprovider.PlayBackValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.PossibleValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.SQLCandiatesValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.ServerGrpValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.ServerValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.TemplateValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.TriggerKeyProvider;
import com.go2wheel.mysqlbackup.valueprovider.UserAccountValueProvider;
import com.go2wheel.mysqlbackup.valueprovider.UserServerGrpValueProvider;

/**
 * copy from {@link StandardAPIAutoConfiguration}
 * @author jianglibo@gmail.com
 *
 */

@Configuration
public class StandardAPIAutoConfigurationMine {

	@Bean
	public ValueProvider serverValueProvider() {
		return new ServerValueProvider();
	}
	
	@Bean
	public ValueProvider keyValueValueProvider() {
		return new KeyValueValueProvider();
	}
	
	
	@Bean
	public ValueProvider candidatesFromSQLProvider() {
		return new SQLCandiatesValueProvider();
	}
	
	
	@Bean
	public ValueProvider ostypeValueProvider() {
		return new OstypeProvider();
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
	public ValueProvider playBackValueProvider() {
		return new PlayBackValueProvider();
	}

	@Bean
	public ValueProvider userServerGrpValueProvider() {
		return new UserServerGrpValueProvider();
	}

	@Bean
	public ValueProvider userAccountValueProvider() {
		return new UserAccountValueProvider();
	}
	
	@Bean
	public ValueProvider tableNameValueProvider() {
		return new DbTableNameProvider();
	}

	@Bean
	public ValueProvider fileValueProvider() {
		return new FileValueProviderMine();
	}
	
	
	@Bean
	public ValueProvider TemplateProvider() {
		return new TemplateValueProvider();
	}

	
	@Bean
	public ValueProvider objectFieldValueProvider() {
		return new ObjectFieldValueProvider();
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
	public ValueProvider triggerKeyProvider() {
		return new TriggerKeyProvider();
	}

	@Bean
	public ValueProvider jobKeyProvider() {
		return new JobKeyProvider();
	}

}
