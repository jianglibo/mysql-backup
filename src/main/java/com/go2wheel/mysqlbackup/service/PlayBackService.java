package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;

@Service
public class PlayBackService {

	@Autowired
	private PlayBackDbService playBackDbService;
	
	@Autowired
	private DSLContext jooq;

	public PlayBack create(Server sourceServer, Server targetServer, String playWhat, List<String> settings) throws UnExpectedInputException {
		if (sourceServer == null || targetServer == null) {
			throw new UnExpectedInputException(null, null, "null source or target server.");
		}
		
		if (!Server.ROLE_GET.equals(sourceServer.getServerRole())) {
			throw new UnExpectedInputException(null, null, "source must a get server.");
		}
		
		if (!Server.ROLE_SET.equals(targetServer.getServerRole())) {
			throw new UnExpectedInputException(null, null, "target must a set server.");
		}
		
		PlayBack pb = new PlayBack();
		pb.setPairs(settings);
		pb.setPlayWhat(playWhat);
		pb.setSourceServerId(sourceServer.getId());
		pb.setTargetServerId(targetServer.getId());
		
		return playBackDbService.save(pb);
	}

	public void remove(PlayBack playback) {
		playBackDbService.delete(playback);
	}
	

}
