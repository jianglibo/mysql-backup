package com.go2wheel.mysqlbackup.convert.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.ServerGrpDbService;
import com.go2wheel.mysqlbackup.service.UserAccountDbService;

public class Id2Model implements ConverterFactory<String, BaseModel> {
	
	
	@Autowired
	private ServerDbService serverDbService;
	@Autowired
	private UserAccountDbService userAccountDbService;
	@Autowired
	private ServerGrpDbService serverGrpDbService;
	

	public <T extends BaseModel> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToModelConverter(targetType);
	}

	private final class StringToModelConverter<T extends BaseModel> implements Converter<String, T> {

		private Class<T> modelType;

		public StringToModelConverter(Class<T> modelType) {
			this.modelType = modelType;
		}

		public T convert(String source) {
			if (modelType == Server.class) {
				return (T) serverDbService.findById(source);
			} else if( modelType == UserAccount.class) {
				return (T) userAccountDbService.findById(source);
			} else if (modelType == ServerGrp.class) {
				return (T) serverGrpDbService.findById(source);
			}
			return (T) null;
		}
	}

}
