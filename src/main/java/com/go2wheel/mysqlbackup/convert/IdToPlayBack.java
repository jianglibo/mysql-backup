package com.go2wheel.mysqlbackup.convert;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.service.PlayBackDbService;
import com.go2wheel.mysqlbackup.util.ObjectUtil;

@Component
public class IdToPlayBack implements Converter<String, PlayBack> {
	
	@Autowired
	private PlayBackDbService playBackDbService;

	@Override
	public PlayBack convert(String source) {
		Optional<String> idOp = ObjectUtil.getValueIfIsToListRepresentation(source, "id");
		if (idOp.isPresent() && !idOp.get().isEmpty()) {
			return playBackDbService.findById(idOp.get());
		} else {
			return playBackDbService.findById(source);
		}
	}

}
