package com.go2wheel.mysqlbackup.service;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.repository.ReusableCronRepository;

@Service
@Validated
public class ReusableCronService {

	@Autowired
	private ReusableCronRepository reusableCronRepository;
	
	
	public ReusableCron save(@Valid ReusableCron reusableCron) {
		return reusableCronRepository.insertAndReturn(reusableCron);
	}
	
	
}
