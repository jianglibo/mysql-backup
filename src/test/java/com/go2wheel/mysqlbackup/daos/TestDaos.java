package com.go2wheel.mysqlbackup.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.model.MailAddress;
import com.go2wheel.mysqlbackup.repository.MailAddressRepository;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestDaos {
	
	
	@Autowired
	private MailAddressRepository mailAddressRepository;
	
	@Before
	public void b() {
		mailAddressRepository.findAll().stream().forEach(ma -> mailAddressRepository.delete(ma));
	}
	
	@Test
	public void t() {
		long num  = mailAddressRepository.count();
		assertThat(num, equalTo(0L));
		MailAddress ma = new MailAddress();
		ma.setDescription("你好");
		ma.setEmail("jk@kk.com");
		mailAddressRepository.insert(ma);
		
		num  = mailAddressRepository.count();
		assertThat(num, equalTo(1L));
	}

}
