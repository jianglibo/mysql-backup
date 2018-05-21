package com.go2wheel.mysqlbackup;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class StartUpFailedReason extends AbstractFailureAnalyzer<Throwable> implements BeanFactoryAware, EnvironmentAware  {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Environment environment;
	
	@SuppressWarnings("unused")
	private BeanFactory beanFactory;

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, Throwable cause) {
		logger.error("{}", environment);
		logger.error("hahahahahahahahahahahahahahahahahahahahahahahahahaha");
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
