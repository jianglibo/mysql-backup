package com.go2wheel.mysqlbackup.convert.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import com.go2wheel.mysqlbackup.dbservice.BorgDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.JobLogDbService;
import com.go2wheel.mysqlbackup.dbservice.KeyValueDbService;
import com.go2wheel.mysqlbackup.dbservice.PlayBackDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.value.Server;

public class Id2Model implements ConverterFactory<String, BaseModel> {
	
	
	@Autowired
	private KeyValueDbService keyValueDbService;
	
	@Autowired
	private JobLogDbService jobLogDbService;
	
	@Autowired
	private BorgDescriptionDbService borgDescriptionDbService;
	
	@Autowired
	private PlayBackDbService playBackDbService;
	
	@Autowired
	private RobocopyDescriptionDbService robocopyDescriptionDbService;
	
	@Autowired
	private RobocopyItemDbService robocopyItemDbService;
	
	@Autowired
	private UserGroupLoader userGroupLoader;

	
	public <T extends BaseModel> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToModelConverter<T>(targetType);
	}

	private final class StringToModelConverter<T extends BaseModel> implements Converter<String, T> {

		private Class<T> modelType;

		public StringToModelConverter(Class<T> modelType) {
			this.modelType = modelType;
		}

		@SuppressWarnings("unchecked")
		public T convert(String source) {
			if (modelType == JobLog.class) {
				return (T) jobLogDbService.findById(source);
			} else if (modelType == KeyValue.class) {
				return (T) keyValueDbService.findById(source);
			} else if (modelType == BorgDescription.class) {
				return (T) borgDescriptionDbService.findById(source);
			} else if (modelType == PlayBack.class) {
				return (T) playBackDbService.findById(source);
			} else if (modelType == RobocopyDescription.class) {
				return (T) robocopyDescriptionDbService.findById(source);
			} else if (modelType == RobocopyItem.class) {
				return (T) robocopyItemDbService.findById(source);
			} else {
				return (T) null;
			}
		}
	}

}
