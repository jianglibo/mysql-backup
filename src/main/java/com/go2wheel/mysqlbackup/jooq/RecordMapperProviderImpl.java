package com.go2wheel.mysqlbackup.jooq;

import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.RecordMapperProvider;
import org.jooq.RecordType;
import org.jooq.impl.DefaultRecordMapper;
import org.springframework.stereotype.Component;

@Component
public class RecordMapperProviderImpl implements RecordMapperProvider {

	@Override
	public <R extends Record, E> RecordMapper<R, E> provide(RecordType<R> recordType, Class<? extends E> type) {
		return new DefaultRecordMapper<>(recordType, type);
	}

}
