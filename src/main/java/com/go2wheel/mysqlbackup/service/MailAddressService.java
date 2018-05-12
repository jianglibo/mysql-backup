package com.go2wheel.mysqlbackup.service;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.model.MailAddress;
import com.go2wheel.mysqlbackup.repository.MailAddressRepository;

@Service
@Validated
public class MailAddressService {

	@Autowired
	private MailAddressRepository mailAddressRepository;
	
	
	public MailAddress save(@Valid MailAddress mailAddress) {
		return mailAddressRepository.insertAndReturn(mailAddress);
	}
	
	
}
