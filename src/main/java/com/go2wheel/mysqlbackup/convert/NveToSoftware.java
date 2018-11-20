package com.go2wheel.mysqlbackup.convert;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.dbservice.SoftwareDbService;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.util.ObjectUtil;

@Component
public class NveToSoftware implements Converter<String, Software> {
	
	@Autowired
	private SoftwareDbService softwareDbService;

	@Override
	public Software convert(String source) {
		Optional<String> idOp = ObjectUtil.getValueIfIsToListRepresentation(source, "id");
		if (idOp.isPresent() && !idOp.get().isEmpty()) {
			return  softwareDbService.findById(idOp.get());
		}
		return  null;
	}

}
