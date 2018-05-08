package com.go2wheel.mysqlbackup;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;

@Component
public class ForFlywayOrderUsage implements  FlywayMigrationStrategy {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	private Scheduler scheduler;
	@Autowired
	public ForFlywayOrderUsage(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void migrate(Flyway flyway) {
		try {
			flyway.migrate();
		} catch (FlywayException e) {
			e.printStackTrace();
			flyway.repair();
		}
		logger.info("fly way called.");
	}

}