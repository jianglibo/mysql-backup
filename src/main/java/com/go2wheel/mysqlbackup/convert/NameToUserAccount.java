package com.go2wheel.mysqlbackup.convert;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.util.ObjectUtil;

@Component
public class NameToUserAccount implements Converter<String, UserAccount> {
	
	@Autowired
	private UserGroupLoader userAccountDbService;

	@Override
	public UserAccount convert(String source) {
		Optional<String> nameOp = ObjectUtil.getValueIfIsToListRepresentation(source, "name");
		if (nameOp.isPresent() && !nameOp.get().isEmpty()) {
			return userAccountDbService.getUserByName(nameOp.get());
		} else {
			return userAccountDbService.getUserByName(source);
		}
	}

}
