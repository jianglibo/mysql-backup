package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.RobocopyItem.ROBOCOPY_ITEM;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.RobocopyItemRecord;
import com.go2wheel.mysqlbackup.model.RobocopyItem;

@Repository
public class JOOQRobocopyItemRepository extends RepositoryBaseImpl<RobocopyItemRecord, RobocopyItem> implements RobocopyItemRepository {

	@Autowired
	protected JOOQRobocopyItemRepository(DSLContext jooq) {
		super(ROBOCOPY_ITEM, RobocopyItem.class, jooq);
	}

	@Override
	public List<RobocopyItem> findByDescriptionId(int descriptionId) {
		return jooq.selectFrom(ROBOCOPY_ITEM).where(ROBOCOPY_ITEM.DESCRIPTION_ID.eq(descriptionId)).fetchInto(RobocopyItem.class);
	}


}
