package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.UserAccount.USER_ACCOUNT;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UserAccountRecord;
import com.go2wheel.mysqlbackup.model.UserAccount;

@Repository
public class JOOQUserAccountRepository extends RepositoryBaseImpl<UserAccountRecord, UserAccount>
		implements UserAccountRepository {
	@Autowired
	protected JOOQUserAccountRepository(DSLContext jooq) {
		super(USER_ACCOUNT, UserAccount.class, jooq);
	}

	@Override
	public UserAccount findByEmail(String email) {
		return jooq.selectFrom(USER_ACCOUNT).where(USER_ACCOUNT.EMAIL.eq(email)).fetchAnyInto(UserAccount.class);
	}

	@Override
	public UserAccount findByMobile(String mobile) {
		return jooq.selectFrom(USER_ACCOUNT).where(USER_ACCOUNT.MOBILE.eq(mobile)).fetchAnyInto(UserAccount.class);
	}

	@Override
	public UserAccount findByName(String name) {
		return jooq.selectFrom(USER_ACCOUNT).where(USER_ACCOUNT.NAME.eq(name)).fetchAnyInto(UserAccount.class);
	}
}
