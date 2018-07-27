package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.google.common.collect.Lists;
import com.jcraft.jsch.Session;

@Service
public class BorgPlayback {
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	/**
	 * list all saved versions, choose one to play back. 
	 * @param sourceServer
	 * @return
	 */
	public List<Path> listLocalRepos(Server sourceServer) {
		Path lrp = settingsInDb.getBorgRepoDir(sourceServer);
		List<Path> pathes = Lists.newArrayList();
		try {
			pathes = Files.list(lrp).collect(Collectors.toList());
			Collections.sort(pathes, (o1, o2) -> {
				try {
					BasicFileAttributes attr1 = Files.readAttributes(o1, BasicFileAttributes.class);
					BasicFileAttributes attr2 = Files.readAttributes(o2, BasicFileAttributes.class);
					return attr1.creationTime().toInstant().compareTo(attr2.creationTime().toInstant());
				} catch (IOException e) {
					return 0;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pathes;
	}
	/**
	 * Copy repo to targetServer. start extract.
	 * 
	 * @param localReop
	 * @param pb
	 */
	public void playback(Session session, Path localReop, PlayBack pb) {
//		ScpUtil.to(session, lfile, rfile);
	}

}
