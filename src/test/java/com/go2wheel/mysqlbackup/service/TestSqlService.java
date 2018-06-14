package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.jooqschema.tables.Author;
import com.go2wheel.mysqlbackup.jooqschema.tables.records.AuthorRecord;
import com.go2wheel.mysqlbackup.util.StringUtil;

public class TestSqlService extends SpringBaseFort {
	
	@Autowired
	private SqlService sqlService;
	
	@Before
	public void bf() {
		jooq.deleteFrom(Author.AUTHOR).execute();
	}

	
	@After
	public void af() {
		jooq.deleteFrom(Author.AUTHOR).execute();
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
