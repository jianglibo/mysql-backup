package com.go2wheel.mysqlbackup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.MyAppSettings;

@Service
public class BorgService {

	@Autowired
	private MyAppSettings myAppSettings;

}
