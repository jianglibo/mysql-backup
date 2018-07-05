package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.jooqschema.tables.Author;
import com.go2wheel.mysqlbackup.model.KeyValue;

public class TestSqlService extends SpringBaseFort {
	
	@Autowired
	private SqlService sqlService;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Before
	public void bf() {
		jooq.deleteFrom(Author.AUTHOR).execute();
	}

	
	@After
	public void af() {
		jooq.deleteFrom(Author.AUTHOR).execute();
	}
	
	@Test
	public void tJdbcTemplate() {
		clearDb();
		KeyValue kv = new KeyValue("a", "b"); 
		keyValueDbService.save(kv);
		String sql = "select ITEM_KEY from KEY_VALUE";
		jdbcTemplate.query(sql, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				String key = rs.getString(1);
				assertThat(key, equalTo("a"));
			}});
		
		List<String> ls = jdbcTemplate.queryForList(sql).stream().flatMap(m -> m.values().stream()).map(Objects::toString).collect(Collectors.toList());
		assertThat(ls.size(), equalTo(1));
		assertThat(ls.get(0), equalTo("a"));
		sql = String.format(BackupCommand.KEY_VALUE_CANDIDATES_SQL, "a");
		ls = jdbcTemplate.queryForList(sql).stream().flatMap(m -> m.values().stream()).map(Objects::toString).collect(Collectors.toList());
		assertThat(ls.size(), equalTo(1));
		assertThat(ls.get(0), equalTo("a"));
	}
	
	@Test
	public void tSelect() {
		Author author = Author.AUTHOR;
		jooq.insertInto(author)
		.set(author.ID, 4)
		.set(author.FIRST_NAME, "Herbert")
		.set(author.LAST_NAME, "Schildt")
		.execute();
		String s = sqlService.select("author", 10);
		assertTrue(s.contains("ID"));
		assertTrue(s.contains("FIRST_NAME"));
		assertTrue(s.contains("LAST_NAME"));
		
		int i = jooq.fetchCount(author);
		assertThat(i, equalTo(1));
		
		sqlService.delete("author", 4);
		i = jooq.fetchCount(author);
		assertThat(i, equalTo(0));
		
	}

}
