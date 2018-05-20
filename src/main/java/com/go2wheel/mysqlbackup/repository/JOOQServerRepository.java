package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.Server.SERVER;

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
		ServerRecord sr = jooq.selectFrom(SERVER).where(SERVER.HOST.eq(host)).fetchOne();
		if (sr != null) {
			return sr.into(Server.class);
		}
		return  null;
	}
}
