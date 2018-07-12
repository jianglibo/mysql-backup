package com.go2wheel.mysqlbackup.util.ui;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.ui.MainMenuGroups;
import com.go2wheel.mysqlbackup.ui.MainMenuGroup;

public class TestMainMenuGroups extends SpringBaseFort {
	
	@Autowired
	private MainMenuGroups mmgs;
	
	@Test
	public void testGroupCount() {
		assertThat(mmgs.getGroups().size(), equalTo(2));
	}
	
	@Test
	public void testAppSettingsCount() {
		MainMenuGroup mg = mmgs.getGroups().stream().filter(g -> g.getName().equals("appmodel")).findFirst().get();
		
		assertThat(mg.getItems().size(), equalTo(3));
	}


}
