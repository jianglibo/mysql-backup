package com.go2wheel.mysqlbackup.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SoftwareRecord;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.repository.SoftwareRepository;

@Service
@Validated
public class SoftwareDbService extends DbServiceBase<SoftwareRecord, Software> {
	

	public SoftwareDbService(SoftwareRepository repo) {
		super(repo);
	}

	public Software findByUniqueField(Software software) {
		return  ((SoftwareRepository)repo).findByUniqueField(software);
	}

}
