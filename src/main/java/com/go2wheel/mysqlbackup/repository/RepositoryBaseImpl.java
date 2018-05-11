package com.go2wheel.mysqlbackup.repository;

import org.jooq.Configuration;
import org.jooq.DAO;
import org.jooq.SQLDialect;
import org.jooq.TableRecord;
import org.jooq.conf.Settings;

public abstract class RepositoryBaseImpl<R extends TableRecord<R>, P, T> implements DAO<R, P, T> {

	@Override
	public Configuration configuration() {
		return null;
	}

	@Override
	public Settings settings() {
		return null;
	}

	@Override
	public SQLDialect dialect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SQLDialect family() {
		// TODO Auto-generated method stub
		return null;
	}

}
