package com.go2wheel.mysqlbackup.convert.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.UserAccountDbService;

public class Id2Model implements ConverterFactory<String, BaseModel> {
	
	
	@Autowired
	private ServerDbService serverDbService;
	@Autowired
	private UserAccountDbService userAccountDbService;
	

	public <T extends BaseModel> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToModelConverter(targetType);
	}

	private final class StringToModelConverter<T extends BaseModel> implements Converter<String, T> {

		private Class<T> modelType;

		public StringToModelConverter(Class<T> modelType) {
			this.modelType = modelType;
		}

		public T convert(String source) {
			T model;
			switch (modelType.getSimpleName()) {
			case "Server":
				model = (T) serverDbService.findById(source);
				return model;
			case "UserAccount":
				model = (T) userAccountDbService.findById(source);
				return model;
			default:
				break;
			}
			return (T) null;
		}
	}

}
