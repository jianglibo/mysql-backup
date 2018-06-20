package com.go2wheel.mysqlbackup.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author jianglibo@gmail.com
 *
 */
public class PlayBack extends BaseModel {
	
	public static final String PLAY_BORG = "BORG";
	public static final String PLAY_MYSQL = "MYSQL";

	private Integer sourceServerId;
	
	private Integer targetServerId;
	
	private String playWhat; // BORG, MYSQL
	
	private List<String> pairs = new ArrayList<>();

	public Integer getSourceServerId() {
		return sourceServerId;
	}

	public void setSourceServerId(Integer sourceServerId) {
		this.sourceServerId = sourceServerId;
	}

	public Integer getTargetServerId() {
		return targetServerId;
	}

	public void setTargetServerId(Integer targetServerId) {
		this.targetServerId = targetServerId;
	}

	public String getPlayWhat() {
		return playWhat;
	}

	public void setPlayWhat(String playWhat) {
		this.playWhat = playWhat;
	}

	public List<String> getPairs() {
		return pairs;
	}

	public void setPairs(List<String> pairs) {
		this.pairs = pairs;
	}
	
	@Override
	public String toListRepresentation(String... fields) {
		if (fields.length == 0) {
			fields = new String[] {"id", "sourceServerId", "targetServerId", "playWhat"};
		}
		return super.toListRepresentation(fields);
	}

}
