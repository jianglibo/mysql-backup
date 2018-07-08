package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.KeyValue;

public class TestDefaultValue extends SpringBaseFort {
	
	@Autowired
	private DefaultValues defaultValues;
	
	@Test
	public void tValue() {
		assertNotNull(defaultValues.getCron().getBorgArchive());
		assertThat(defaultValues.getCron().getCommon().size(), equalTo(10));
	}
	
	@Test
	public void testDefaultCount() {
		clearDb();
		KeyValueProperties kvp = defaultValues.getDefaultCount();
		assertThat(kvp.getInteger(DefaultValues.BORG_DOWNLOAD_CN), equalTo(20));
		assertThat(kvp.getInteger(DefaultValues.JOB_LOG_CN), equalTo(20));
		assertThat(kvp.getInteger(DefaultValues.MYSQL_DUMP_CN), equalTo(2));
		assertThat(kvp.getInteger(DefaultValues.MYSQL_FLUSH_CN), equalTo(10));
		assertThat(kvp.getInteger(DefaultValues.SERVER_STATE_CN), equalTo(2));
		assertThat(kvp.getInteger(DefaultValues.STORAGE_STATE_CN), equalTo(7));
	}
	
	
	@Test
	public void testDboverride() {
		clearDb(); // if clearDb defaultValues.getDefaultCount() will be empty.
		KeyValueProperties kvp = defaultValues.getDefaultCount();
		
		assertThat(kvp.getInteger(DefaultValues.BORG_DOWNLOAD_CN), equalTo(20));
		
		Optional<KeyValue> kvInProperties = kvp.getKeyValue(DefaultValues.BORG_DOWNLOAD_CN);
		
		if (!kvInProperties.isPresent()) {
			KeyValue kv = new KeyValue(DefaultValues.DEFAULT_COUNT_PREFIX + "." + DefaultValues.BORG_DOWNLOAD_CN, "20");
			keyValueDbService.save(kv);
		}
		
		// must get defaultCount again. because it's replaced by new one.
		kvp = defaultValues.getDefaultCount();
		
		kvInProperties = kvp.getKeyValue(DefaultValues.BORG_DOWNLOAD_CN);
		
		assertTrue(kvInProperties.isPresent());
		
		assertThat(kvInProperties.get().getItemValue(), equalTo("20"));

		kvInProperties.get().setItemValue("40");

		keyValueDbService.save(kvInProperties.get());
		
		kvInProperties = kvp.getKeyValue(DefaultValues.BORG_DOWNLOAD_CN);
		assertThat(kvInProperties.get().getItemValue(), equalTo("40"));

		KeyValue kvInDb = keyValueDbService.findOneByKey(DefaultValues.DEFAULT_COUNT_PREFIX, DefaultValues.BORG_DOWNLOAD_CN);

		kvInDb.setItemValue("60");
		keyValueDbService.save(kvInDb);

		kvp = defaultValues.getDefaultCount();
		assertThat(kvp.getInteger(DefaultValues.BORG_DOWNLOAD_CN), equalTo(60));
	}
}
