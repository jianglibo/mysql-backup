package com.go2wheel.mysqlbackup.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponentsBuilder;

@RequestScope
@Component
public class MenuService {

	@Autowired
	private UriComponentsBuilder uriComponentsBuilder;
	
	
	
}
