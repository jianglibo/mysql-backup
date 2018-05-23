package com.go2wheel.mysqlbackup;


import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.go2wheel.mysqlbackup.util.ExceptionUtil;

public class StartUpFailedReason extends AbstractFailureAnalyzer<Throwable> implements BeanFactoryAware, EnvironmentAware  {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Environment environment;
	
	@SuppressWarnings("unused")
	private BeanFactory beanFactory;

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, Throwable cause) {
		if (rootFailure.getMessage().contains("org.quartz.JobPersistenceException")) {
			DataSource ds = beanFactory.getBean(DataSource.class);
			String bl = environment.getProperty("spring.flyway.baseline-version");
			Flyway flyway = new Flyway();
			flyway.setDataSource(ds);
//			flyway.
//			flyway.migrate();
		} 
		ExceptionUtil.logThrowable(logger, rootFailure);
		return null;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

}
