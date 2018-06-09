package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.Server.SERVER;

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
//		return jooq.selectFrom(SERVER).fetch(SERVER.OS);
//		return jooq.selectDistinct(SERVER.OS).fetch(SERVER.OS);
	}
}
