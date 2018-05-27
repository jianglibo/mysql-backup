package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.Server.SERVER;
import static com.go2wheel.mysqlbackup.jooqschema.tables.ServerGrp.SERVER_GRP;
import static com.go2wheel.mysqlbackup.jooqschema.tables.ServergrpAndServer.SERVERGRP_AND_SERVER;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ServerGrpRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;

@Repository
public class JOOQServerGrpRepository extends RepositoryBaseImpl<ServerGrpRecord, ServerGrp>
		implements ServerGrpRepository {

	@Autowired
	protected JOOQServerGrpRepository(DSLContext jooq) {
		super(SERVER_GRP, ServerGrp.class, jooq);
	}

	@Override
	public void addServer(ServerGrp serverGrp, Server server) {
		jooq.insertInto(SERVERGRP_AND_SERVER).set(SERVERGRP_AND_SERVER.GRP_ID, serverGrp.getId())
				.set(SERVERGRP_AND_SERVER.SERVER_ID, server.getId()).execute();
	}

	@Override
	public List<Server> getServers(ServerGrp serverGrp) {
		return jooq.select(SERVER.fields()).from(SERVER).join(SERVERGRP_AND_SERVER)
				.on(SERVER.ID.eq(SERVERGRP_AND_SERVER.SERVER_ID))
				.where(SERVERGRP_AND_SERVER.GRP_ID.eq(serverGrp.getId())).fetch().into(Server.class);

	}

	@Override
	public void removeServer(ServerGrp serverGrp, Server server) {
		removeServer(serverGrp, server.getId());
	}

	@Override
	public void removeServer(ServerGrp serverGrp, int serverId) {
		jooq.deleteFrom(SERVERGRP_AND_SERVER).where(SERVERGRP_AND_SERVER.GRP_ID.eq(serverGrp.getId())
				.and(SERVERGRP_AND_SERVER.SERVER_ID.eq(serverId))).execute();
	}

}
