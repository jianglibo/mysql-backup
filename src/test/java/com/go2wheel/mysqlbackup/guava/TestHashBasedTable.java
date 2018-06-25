package com.go2wheel.mysqlbackup.guava;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class TestHashBasedTable {
	


	@Test
	public void givenTable_whenGet_returnsSuccessfully() {
		
	    Table<String, String, String> dfRootByDate 
	      = HashBasedTable.create();
	    dfRootByDate.put("/", "2018-06-25", "5%");
	    dfRootByDate.put("/", "2018-06-26", "36%");
	    dfRootByDate.put("/", "2018-06-27", "60%");
	    
	    Map<String, String> byDate = dfRootByDate.row("/");
	    
	    
	 
	    String pct 
	      = dfRootByDate.get("/", "2018-06-25");
	    String pctNot 
	      = dfRootByDate.get("Oxford", "IT");
	 
	    assertThat(pct, equalTo("5%"));
	    assertNull(pctNot);
	}
	
	@Test
	public void givenTable_whenContains_returnsSuccessfully() {
	    Table<String, String, Integer> universityCourseSeatTable 
	      = HashBasedTable.create();
	    universityCourseSeatTable.put("Mumbai", "Chemical", 120);
	    universityCourseSeatTable.put("Mumbai", "IT", 60);
	    universityCourseSeatTable.put("Harvard", "Electrical", 60);
	    universityCourseSeatTable.put("Harvard", "IT", 120);
	 
	    boolean entryIsPresent
	      = universityCourseSeatTable.contains("Mumbai", "IT");
	    boolean courseIsPresent 
	      = universityCourseSeatTable.containsColumn("IT");
	    boolean universityIsPresent 
	      = universityCourseSeatTable.containsRow("Mumbai");
	    boolean seatCountIsPresent 
	      = universityCourseSeatTable.containsValue(60);
	 
	    assertTrue(entryIsPresent);
	    assertTrue(courseIsPresent);
	    assertTrue(universityIsPresent);
	    assertTrue(seatCountIsPresent);
	}
}
