package com.go2wheel.mysqlbackup.jooq;

import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.RecordMapperProvider;
import org.jooq.RecordType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

//@Configuration
//@AutoConfigureAfter({ DataSourceAutoConfiguration.class,
//	HibernateJpaAutoConfiguration.class })
public class JooqConfiguration implements ApplicationContextAware {
	
	
	@Bean
	public RecordMapperProvider recordMapperProvider() {
		return new RecordMapperProvider() {
			
			@Override
			public <R extends Record, E> RecordMapper<R, E> provide(RecordType<R> recordType, Class<? extends E> type) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		
		
	}
	
//	@Autowired
//	private DataSource dataSource;
//	
//	@Bean
//	public DefaultConfiguration jooqDefaultConfiguration() {
//	    DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
//	    jooqConfiguration.set(connectionProvider());
//	    jooqConfiguration.set(new DefaultExecuteListenerProvider(exceptionTransformer()));
////	    jooqConfiguration.setTransactionProvider(transactionProvider());
//	 
////	    String sqlDialectName = environment.getRequiredProperty("jooq.sql.dialect");
////	    SQLDialect dialect = SQLDialect.valueOf();
//	    jooqConfiguration.set(SQLDialect.HSQLDB);
//	 
//	    return jooqConfiguration;
//	}
	
//	@Bean
//	public DefaultDSLContext dsl() {
//		return new DefaultDSLContext(jooqDefaultConfiguration());
//	}
//	
//	@Bean
//	public ExceptionTranslator exceptionTransformer() {
//	    return new ExceptionTranslator();
//	}
	
//	@Bean
//	public TransactionAwareDataSourceProxy transactionAwareDataSource() {
//	    return ;
//	}
	
//	@Bean
//	public TransactionProvider transactionProvider() {
//		return new SpringTransactionProvider(transactionManager());
//	}
	 
//	@Bean
//	public DataSourceTransactionManager transactionManager() {
//	    return new DataSourceTransactionManager(dataSource);
//	}
	 
//	@Bean
//	public DataSourceConnectionProvider connectionProvider() {
//	    return new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource));
//	}
	 
	
//	ConnectionProvider
//	TransactionProvider
//	RecordMapperProvider
//	RecordUnmapperProvider
//	RecordListenerProvider
//	ExecuteListenerProvider
//	VisitListenerProvider
	
}
