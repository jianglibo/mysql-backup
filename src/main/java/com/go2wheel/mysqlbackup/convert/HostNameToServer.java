package com.go2wheel.mysqlbackup.convert;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ObjectUtil;

@Component
public class HostNameToServer implements Converter<String, Server> {
	
	@Autowired
	private ServerDbService serverDbService;

	@Override
	public Server convert(String source) {
		Optional<String> idOp = ObjectUtil.getValueIfIsToListRepresentation(source, "id");
		if (idOp.isPresent() && !idOp.get().isEmpty()) {
			return serverDbService.loadFull(serverDbService.findById(idOp.get()));
		} else {
			return serverDbService.loadFull(serverDbService.findByHost(source));
		}
	}

}
