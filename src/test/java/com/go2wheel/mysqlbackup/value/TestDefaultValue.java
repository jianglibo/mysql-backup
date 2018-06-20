package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;

public class TestDefaultValue extends SpringBaseFort {
	
	@Autowired
	private DefaultValues defaultValues;
	
	@Test
	public void tValue() {
		assertNotNull(defaultValues.getCron().getBorgArchive());
		assertThat(defaultValues.getCron().getCommon().size(), equalTo(10));
	}

}
