package com.go2wheel.mysqlbackup;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.go2wheel.mysqlbackup.dbservice.TableDiscovery;

public class TestJdbcTemplate extends SpringBaseFort {

	@Autowired
	private JdbcTemplate template;
	
	@Autowired
	protected TableDiscovery tableDiscovery;
	

	
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
