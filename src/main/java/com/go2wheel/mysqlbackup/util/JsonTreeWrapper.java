package com.go2wheel.mysqlbackup.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

public class JsonTreeWrapper {
	
	private static Pattern ARY_PTN = Pattern.compile("^(.*?)\\[(\\d+)\\]");
	
	private JsonNode root;
	
	public JsonTreeWrapper(JsonNode root) {
		this.root = root;
	}
	
	public boolean nodeValueEqual(String dotpath, String value) {
		String[] parts = dotpath.split("\\.");
		JsonNode tn = this.root;
		for(String part: parts) {
			tn = getNode(tn, part);
			if (tn == null) {
				return false;
			}
			if (tn instanceof NullNode) {
				return value == null; 
			}
		}
		return value.equals(tn.asText());
	}

	private JsonNode getNode(JsonNode tn, String part) {
		Matcher m = ARY_PTN.matcher(part);
		if (m.matches()) {
			tn = tn.get(m.group(1));
			tn = tn.get(Integer.parseInt(m.group(2)));
		} else {
			tn = tn.get(part);
		}
		return tn;
	}
	
	public boolean nodeExists(String...dotpaths) {
		for(String dotpath: dotpaths) {
			String[] parts = dotpath.split("\\.");
			JsonNode tn = this.root;
			for(String part: parts) {
				tn = getNode(tn, part);
				if (tn == null) {
					return false;
				}
			}
		}
		return true;
	}

}
