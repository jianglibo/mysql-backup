package com.go2wheel.mysqlbackup.resulthandler;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TestFacadeResultHandler {
	
	
	@Test
	public void tCollection() {
		List<String> sl = new ArrayList<>();
		sl.add("abc");
		
		assertTrue("list is a collection", sl instanceof Collection);
		
		assertTrue("list is a list", sl instanceof List);
		
		Object o = ((Collection)sl).iterator().next();
		
		assertTrue("content is string.", o instanceof String);
		
		Map<String, String> map = new HashMap<>();
		assertFalse("map is not a collection", map instanceof Collection);
		
		assertTrue("map is a map", map instanceof Map);
	}
	
	public void tListOfString() {
		
	}

}
