package com.go2wheel.mysqlbackup.convert;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.service.SubscribeDbService;
import com.go2wheel.mysqlbackup.util.ObjectUtil;

@Component
public class IdToUserServerGrp implements Converter<String, Subscribe> {
	
	@Autowired
	private SubscribeDbService userServerGrpDbService;

	@Override
	public Subscribe convert(String source) {
		Optional<String> idOp = ObjectUtil.getValueIfIsToListRepresentation(source, "id");
		if (idOp.isPresent() && !idOp.get().isEmpty()) {
			return userServerGrpDbService.findById(idOp.get());
		} else {
			return userServerGrpDbService.findById(source);
		}
	}

}
