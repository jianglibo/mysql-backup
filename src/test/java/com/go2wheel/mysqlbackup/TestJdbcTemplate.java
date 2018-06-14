package com.go2wheel.mysqlbackup;

import static com.go2wheel.mysqlbackup.jooqschema.tables.MysqlInstance.MYSQL_INSTANCE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jooq.DSLContext;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.service.TableDiscovery;

public class TestJdbcTemplate extends SpringBaseFort {

	@Autowired
	private JdbcTemplate template;
	
	@Autowired
	protected DSLContext jooq;
	
	@Autowired
	protected TableDiscovery tableDiscovery;
	
	@Test
	public void tj() {
		List<MysqlInstance> msqls = jooq.selectFrom(MYSQL_INSTANCE).fetchInto(MysqlInstance.class);
		assertNotNull(msqls);
	}
	
	@Test
	public void classFind() throws ClassNotFoundException {
		Object o = tableDiscovery.getTable("author");
		assertThat(o.getClass().getSimpleName(), equalTo("Author"));

	}
	
//	@Test
//	public void t() throws SQLException {
//		ResultSetExtractor<Long> rl = new ResultSetExtractor<Long>(){
//			@Override
//			public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
//				return rs.getLong(0);
//			}
//		};
//		
//		Long l = template.query("SELECT count(*) from PUBLIC.MYSQL_INSTANCE", rl);
//		assertThat(l, greaterThan(0L));
//	}
}
