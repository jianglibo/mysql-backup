package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.Subscribe.SUBSCRIBE;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SubscribeRecord;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.Subscribe;

@Repository
public class JOOQSubscribeRepository extends RepositoryBaseImpl<SubscribeRecord, Subscribe>
		implements SubscribeRepository {

	@Autowired
	protected JOOQSubscribeRepository(DSLContext jooq) {
		super(SUBSCRIBE, Subscribe.class, jooq);
	}

	@Override
	public List<Subscribe> findByUser(UserAccount ua) {
		return jooq.selectFrom(SUBSCRIBE).where(SUBSCRIBE.USER_ACCOUNT_ID.eq(ua.getId())).fetch()
				.into(Subscribe.class);
	}

	@Override
	public Subscribe findByUserAndServerGrp(@NotNull UserAccount user, ServerGrp serverGroup) {
		return jooq.selectFrom(SUBSCRIBE)
				.where(SUBSCRIBE.USER_ACCOUNT_ID.eq(user.getId())
						.and(SUBSCRIBE.SERVER_GRP_ID.eq(serverGroup.getId())))
				.fetchAnyInto(Subscribe.class);
	}

}
