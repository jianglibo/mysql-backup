package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.Server.SERVER;
import static com.go2wheel.mysqlbackup.jooqschema.tables.ServergrpAndServer.SERVERGRP_AND_SERVER;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ServerRecord;
import com.go2wheel.mysqlbackup.model.Server;

@Repository
public class JOOQServerRepository extends RepositoryBaseImpl<ServerRecord, Server> implements ServerRepository {
	@Autowired
	protected JOOQServerRepository(DSLContext jooq) {
		super(SERVER, Server.class, jooq);
	}

	@Override
	public Server findByHost(String host) {
		return jooq.selectFrom(SERVER).where(SERVER.HOST.eq(host)).fetchAnyInto(Server.class);
	}

	@Override
	public List<Server> findLikeHost(String partOfHostName) {
		String likeStr = partOfHostName.indexOf('%') == -1 ? '%' + partOfHostName + '%' : partOfHostName;
		return jooq.selectFrom(SERVER).where(SERVER.HOST.likeIgnoreCase(likeStr)).fetchInto(Server.class);
	}

	@Override
	public List<String> findDistinctOsType(String input) {
		return jooq.selectDistinct(SERVER.OS).from(SERVER).fetch(SERVER.OS);
	}

	@Override
	public List<Server> findLikeHostAndRoleIs(String partOfHostName, String role) {
		String likeStr = partOfHostName.indexOf('%') == -1 ? '%' + partOfHostName + '%' : partOfHostName;
		return jooq.selectFrom(SERVER).where(SERVER.HOST.likeIgnoreCase(likeStr).and(SERVER.SERVER_ROLE.eq(role)))
				.fetchInto(Server.class);
	}

	@Override
	public Object findByGrpId(Integer grpId) {
		return jooq.select(SERVER.fields()).from(SERVER).join(SERVERGRP_AND_SERVER)
				.on(SERVER.ID.eq(SERVERGRP_AND_SERVER.SERVER_ID))
				.where(SERVERGRP_AND_SERVER.GRP_ID.eq(grpId)).fetch().into(Server.class);
	}

}
