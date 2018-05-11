package com.go2wheel.mysqlbackup.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.model.MailAddress;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.repository.MailAddressRepository;
import com.go2wheel.mysqlbackup.repository.ReusableCronRepository;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestDaos {
	
	
	@Autowired
	private MailAddressRepository mailAddressRepository;
	
	@Autowired
	private ReusableCronRepository reusableCronRepository;
	
	@Before
	public void b() {
		mailAddressRepository.findAll().stream().forEach(ma -> mailAddressRepository.delete(ma));
		reusableCronRepository.findAll().stream().forEach(recron -> reusableCronRepository.delete(recron));
	}
	
	@Test
	public void tMailAddress() {
		long num  = mailAddressRepository.count();
		assertThat(num, equalTo(0L));
		
		MailAddress ma = new MailAddress();
		ma.setDescription("你好");
		ma.setEmail("jk@kk.com");
		mailAddressRepository.insert(ma);
		
		num  = mailAddressRepository.count();
		assertThat(num, equalTo(1L));
		
		ma = new MailAddress();
		ma.setDescription("你好");
		ma.setEmail("jk@kk.com");
		mailAddressRepository.insert(ma);
		
		num  = mailAddressRepository.count();
		assertThat(num, equalTo(2L));

		
		assertThat(ma.getId(), greaterThan(0));
	}
	
	@Test
	public void tCronNoValidate() {
		long num  = reusableCronRepository.count();
		assertThat(num, equalTo(0L));
		ReusableCron ma = new ReusableCron();
		ma.setDescription("你好");
		ma.setExpression("hello");
		reusableCronRepository.insert(ma);
		num  = reusableCronRepository.count();
		assertThat(num, equalTo(1L));
		assertThat(ma.getId(), greaterThan(0));
	}
	
	@Test(expected = ParseException.class)
	public void tCronValidate() throws ParseException {
		long num  = reusableCronRepository.count();
		assertThat(num, equalTo(0L));
		ReusableCron ma = new ReusableCron();
		ma.setDescription("你好");
		ma.setExpression("hello");
		reusableCronRepository.insertAfterValidate(ma);
	}


}
