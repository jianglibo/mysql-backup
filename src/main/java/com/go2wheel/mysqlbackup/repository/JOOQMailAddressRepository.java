package com.go2wheel.mysqlbackup.repository;

import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.go2wheel.mysqlbackup.exception.EntityNotFoundException;
import com.go2wheel.mysqlbackup.jooqschema.tables.Mailaddress;
import com.go2wheel.mysqlbackup.jooqschema.tables.records.MailaddressRecord;
import com.go2wheel.mysqlbackup.model.MailAddress;

@Repository
public class JOOQMailAddressRepository implements MailAddressRepository {
	
	private DSLContext jooq;
	
	@Autowired
	public JOOQMailAddressRepository(DSLContext jooq) {
		this.jooq = jooq;
	}
	
    private MailaddressRecord createRecord(MailAddress todoEntry) {
    	MailaddressRecord record = new MailaddressRecord();
        record.setDescription(todoEntry.getDescription());
        record.setEmail(todoEntry.getEmail());
        return record;
    }
	
	
    private MailAddress convertQueryResultToModelObject(MailaddressRecord queryResult) {
        return queryResult.into(MailAddress.class);
    }

	@Override
	public MailAddress add(MailAddress mailAddressEntry) {
		MailaddressRecord persisted = jooq.insertInto(Mailaddress.MAILADDRESS)
                .set(createRecord(mailAddressEntry))
                .returning()
                .fetchOne();
 
        return convertQueryResultToModelObject(persisted);
	}

	@Override
	@Transactional
	public MailAddress delete(int id) throws EntityNotFoundException {
		MailAddress deleted = findById(id);
        int deletedRecordCount = jooq.delete(Mailaddress.MAILADDRESS)
                .where(Mailaddress.MAILADDRESS.ID.equal(id))
                .execute();
        return deleted;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MailAddress> findAll() {
        List<MailAddress> todoEntries = new ArrayList<>();
        
        List<MailaddressRecord> queryResults = jooq.selectFrom(Mailaddress.MAILADDRESS).fetchInto(MailaddressRecord.class);
 
        for (MailaddressRecord queryResult: queryResults) {
        	MailAddress todoEntry = convertQueryResultToModelObject(queryResult);
            todoEntries.add(todoEntry);
        }
 
        return todoEntries;
	}

    @Transactional(readOnly = true)
    @Override
	public MailAddress findById(int id) throws EntityNotFoundException {
    	MailaddressRecord queryResult = jooq.selectFrom(Mailaddress.MAILADDRESS)
                .where(Mailaddress.MAILADDRESS.ID.equal(id))
                .fetchOne();
 
        if (queryResult == null) {
            throw new EntityNotFoundException(MailAddress.class, id);
        }
 
        return convertQueryResultToModelObject(queryResult);
	}

	@Override
	public MailAddress update(MailAddress MailAddressEntry) {
		return null;
	}

}
