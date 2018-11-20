package com.go2wheel.mysqlbackup.convert.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import com.go2wheel.mysqlbackup.dbservice.BorgDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.JobLogDbService;
import com.go2wheel.mysqlbackup.dbservice.KeyValueDbService;
import com.go2wheel.mysqlbackup.dbservice.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.dbservice.PlayBackDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerGrpDbService;
import com.go2wheel.mysqlbackup.dbservice.SoftwareDbService;
import com.go2wheel.mysqlbackup.dbservice.SubscribeDbService;
import com.go2wheel.mysqlbackup.dbservice.UserAccountDbService;
import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.model.UserAccount;

public class Id2Model implements ConverterFactory<String, BaseModel> {
	
	
	@Autowired
	private ServerDbService serverDbService;
	@Autowired
	private UserAccountDbService userAccountDbService;
	@Autowired
	private ServerGrpDbService serverGrpDbService;
	
	@Autowired
	private SubscribeDbService subscribeDbService;
	
	@Autowired
	private MysqlInstanceDbService mysqlInstanceDbService;
	
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
	private SoftwareDbService softwareDbService;

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
			if (modelType == Server.class) {
				return (T) serverDbService.findById(source);
			} else if( modelType == UserAccount.class) {
				return (T) userAccountDbService.findById(source);
			} else if (modelType == ServerGrp.class) {
				return (T) serverGrpDbService.findById(source);
			} else if (modelType == Subscribe.class) {
				return (T) subscribeDbService.findById(source);
			} else if (modelType == JobLog.class) {
				return (T) jobLogDbService.findById(source);
			} else if (modelType == MysqlInstance.class) {
				return (T) mysqlInstanceDbService.findById(source);
			} else if (modelType == KeyValue.class) {
				return (T) keyValueDbService.findById(source);
			} else if (modelType == BorgDescription.class) {
				return (T) borgDescriptionDbService.findById(source);
			} else if (modelType == Software.class) {
				return (T) softwareDbService.findById(source);
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
