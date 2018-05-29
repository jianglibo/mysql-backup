package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.UserAndServerGrp.USER_AND_SERVER_GRP;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UserAndServerGrpRecord;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;

@Repository
public class JOOQUserServerGrpRepository extends RepositoryBaseImpl<UserAndServerGrpRecord, UserServerGrp>
		implements UserServerGrpRepository {

	@Autowired
	protected JOOQUserServerGrpRepository(DSLContext jooq) {
		super(USER_AND_SERVER_GRP, UserServerGrp.class, jooq);
	}

	@Override
	public List<UserServerGrp> findByUser(UserAccount ua) {
		return jooq.selectFrom(USER_AND_SERVER_GRP).where(USER_AND_SERVER_GRP.USER_ACCOUNT_ID.eq(ua.getId())).fetch()
				.into(UserServerGrp.class);
	}

	@Override
	public UserServerGrp findByUserAndServerGrp(@NotNull UserAccount user, ServerGrp serverGroup) {
		return jooq.selectFrom(USER_AND_SERVER_GRP)
				.where(USER_AND_SERVER_GRP.USER_ACCOUNT_ID.eq(user.getId())
						.and(USER_AND_SERVER_GRP.SERVER_GRP_ID.eq(serverGroup.getId())))
				.fetchAnyInto(UserServerGrp.class);
	}

}
