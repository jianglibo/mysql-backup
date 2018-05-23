package com.go2wheel.mysqlbackup;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.util.ExceptionUtil;

@Component
public class ForFlywayOrderUsage implements  FlywayMigrationStrategy {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void migrate(Flyway flyway) {
		try {
			flyway.migrate();
		} catch (FlywayException e) {
			ExceptionUtil.logErrorException(logger, e);
			flyway.repair();
		}
		logger.info("fly way called.");
	}

}
