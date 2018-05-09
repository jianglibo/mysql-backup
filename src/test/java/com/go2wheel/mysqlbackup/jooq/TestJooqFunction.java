package com.go2wheel.mysqlbackup.jooq;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.jooqschema.tables.Author;
import com.go2wheel.mysqlbackup.jooqschema.tables.AuthorBook;
import com.go2wheel.mysqlbackup.jooqschema.tables.Book;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestJooqFunction {

	// http://www.jooq.org/doc/3.10/manual/getting-started/tutorials/jooq-with-spring/
	Author author = Author.AUTHOR;
	Book book = Book.BOOK;
	AuthorBook authorBook = AuthorBook.AUTHOR_BOOK;

	@Before
	public void before() {
		create.selectFrom(authorBook).fetch().stream().forEach(br -> {
			br.delete();
		});

		create.selectFrom(book).fetch().stream().forEach(br -> {
			br.delete();
		});

		create.selectFrom(author).fetch().stream().forEach(br -> {
			br.delete();
		});
	}

	@Autowired
	private DSLContext create;

	//@formatter:off
	@Test
	public void tTables() {
		create.insertInto(author)
			.set(author.ID, 4)
			.set(author.FIRST_NAME, "Herbert")
			.set(author.LAST_NAME, "Schildt")
			.execute();
		
		create.insertInto(book)
			.set(book.ID, 4)
			.set(book.TITLE, "A Beginner's Guide")
			.execute();
		
		create.insertInto(authorBook)
			.set(authorBook.AUTHOR_ID, 4)
			.set(authorBook.BOOK_ID, 4)
			.execute();

		Result<Record2<String, Integer>> result = 
				create.select(author.LAST_NAME, DSL.count())
				.from(author)
				.join(authorBook).on(author.ID.equal(authorBook.AUTHOR_ID))
				.join(book).on(authorBook.BOOK_ID.equal(book.ID))
				.groupBy(author.LAST_NAME).fetch();

	}

	@Test(expected = DataAccessException.class)
	public void givenInvalidData_whenInserting_thenFail() {
		create.insertInto(authorBook)
			.set(authorBook.AUTHOR_ID, 4)
			.set(authorBook.BOOK_ID, 5)
			.execute();
	}

	// deleting a none exist record is not an error.
	@Test
	public void givenInvalidData_whenDeleting_thenFail() {
		create.delete(book)
			.where(book.ID.equal(1))
			.execute();
	}

}
