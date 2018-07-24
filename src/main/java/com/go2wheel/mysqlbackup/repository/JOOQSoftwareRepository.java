package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.Software.SOFTWARE;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SoftwareRecord;
import com.go2wheel.mysqlbackup.model.Software;

@Repository
public class JOOQSoftwareRepository extends RepositoryBaseImpl<SoftwareRecord, Software> implements SoftwareRepository {

	@Autowired
	protected JOOQSoftwareRepository(DSLContext jooq) {
		super(SOFTWARE, Software.class, jooq);
	}

	@Override
	public Software findByUniqueField(Software software) {
		return jooq.selectFrom(SOFTWARE).where(SOFTWARE.NAME.eq(software.getName())
				.and(SOFTWARE.VERSION.eq(software.getVersion()).and(SOFTWARE.TARGET_ENV.eq(software.getTargetEnv()))))
				.fetchOneInto(Software.class);
	}

	@Override
	public List<Software> findByName(String name) {
		return jooq.selectFrom(SOFTWARE).where(SOFTWARE.NAME.eq(name)).fetchInto(Software.class);
	}

}
