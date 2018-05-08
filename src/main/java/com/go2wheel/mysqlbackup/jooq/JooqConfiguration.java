package com.go2wheel.mysqlbackup.jooq;

import javax.sql.DataSource;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

@Configuration
public class JooqConfiguration {
	
	@Bean
	public DefaultConfiguration jooqDefaultConfiguration(DataSourceConnectionProvider connectionProvider) {
	    DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
	    jooqConfiguration.set(connectionProvider);
	    jooqConfiguration.set(new DefaultExecuteListenerProvider(exceptionTransformer()));
	 
//	    String sqlDialectName = environment.getRequiredProperty("jooq.sql.dialect");
//	    SQLDialect dialect = SQLDialect.valueOf();
	    jooqConfiguration.set(SQLDialect.HSQLDB);
	 
	    return jooqConfiguration;
	}
	
	@Bean
	public DefaultDSLContext dsl(DefaultConfiguration jooqDefaultConfiguration) {
		return new DefaultDSLContext(jooqDefaultConfiguration);
	}
	
	@Bean
	public ExceptionTranslator exceptionTransformer() {
	    return new ExceptionTranslator();
	}
	
	@Bean
	public TransactionAwareDataSourceProxy transactionAwareDataSource(DataSource dataSource) {
	    return new TransactionAwareDataSourceProxy(dataSource);
	}
	 
	@Bean
	public DataSourceTransactionManager transactionManager(DataSource dataSource) {
	    return new DataSourceTransactionManager(dataSource);
	}
	 
	@Bean
	public DataSourceConnectionProvider connectionProvider(TransactionAwareDataSourceProxy tadsp) {
	    return new DataSourceConnectionProvider(tadsp);
	}
	 
	
//	ConnectionProvider
//	TransactionProvider
//	RecordMapperProvider
//	RecordUnmapperProvider
//	RecordListenerProvider
//	ExecuteListenerProvider
//	VisitListenerProvider
	
}
