package com.go2wheel.mysqlbackup.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.text.ParseException;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.MailAddress;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.repository.MailAddressRepository;
import com.go2wheel.mysqlbackup.repository.ReusableCronRepository;
import com.go2wheel.mysqlbackup.service.MailAddressService;
import com.go2wheel.mysqlbackup.service.ReusableCronService;

public class TestDaos extends SpringBaseFort {
	
	@Autowired
	private MailAddressService mailAddressService;
	
	@Autowired
	private ReusableCronService reusableCronService;
	
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
		ma = mailAddressService.save(ma);
		
		num  = mailAddressRepository.count();
		assertThat(num, equalTo(1L));
		
		ma = new MailAddress();
		ma.setDescription("你好");
		ma.setEmail("jk1@kk.com");
		ma = mailAddressService.save(ma);
		
		num  = mailAddressRepository.count();
		assertThat(num, equalTo(2L));
		assertThat(ma.getId(), greaterThan(0));
	}
	
	@Test(expected = DuplicateKeyException.class)
	public void tEmailUnique() {
		long num  = mailAddressRepository.count();
		assertThat(num, equalTo(0L));
		
		MailAddress ma = new MailAddress();
		ma.setDescription("你好");
		ma.setEmail("jk@kk.com");
		mailAddressService.save(ma);
		
		num  = mailAddressRepository.count();
		assertThat(num, equalTo(1L));
		
		ma = new MailAddress();
		ma.setDescription("你好");
		ma.setEmail("jk@kk.com");
		mailAddressService.save(ma);
		
		num  = mailAddressRepository.count();
		assertThat(num, equalTo(2L));
		assertThat(ma.getId(), greaterThan(0));
	}
	
	@Test(expected = ConstraintViolationException.class)
	public void tEmailUnvalidate() {
		long num  = mailAddressRepository.count();
		assertThat(num, equalTo(0L));
		
		MailAddress ma = new MailAddress();
		ma.setDescription("你好");
		ma.setEmail("kk.com");
		mailAddressService.save(ma);
	}
	
	@Test(expected = ConstraintViolationException.class)
	public void tCronExpressionEmpty() {
		long num  = reusableCronRepository.count();
		assertThat(num, equalTo(0L));
		ReusableCron ma = new ReusableCron("", "你好");
		ma = reusableCronService.save(ma);
		num  = reusableCronRepository.count();
		assertThat(num, equalTo(1L));
		assertThat(ma.getId(), greaterThan(0));
	}
	
	@Test(expected = ConstraintViolationException.class)
	public void tCronExpressionNull() {
		long num  = reusableCronRepository.count();
		assertThat(num, equalTo(0L));
		ReusableCron ma = new ReusableCron(null, "你好");
		ma = reusableCronService.save(ma);
		num  = reusableCronRepository.count();
		assertThat(num, equalTo(1L));
		assertThat(ma.getId(), greaterThan(0));
	}

	@Test(expected = ConstraintViolationException.class)
	public void tCronNoValidate() {
		long num  = reusableCronRepository.count();
		assertThat(num, equalTo(0L));
		ReusableCron ma = new ReusableCron("hello", "你好");
		ma = reusableCronService.save(ma);
		num  = reusableCronRepository.count();
		assertThat(num, equalTo(1L));
		assertThat(ma.getId(), greaterThan(0));
	}
	
	@Test()
	public void tCronValidate() throws ParseException {
		long num  = reusableCronRepository.count();
		assertThat(num, equalTo(0L));
		ReusableCron ma = new ReusableCron("0 30 6,12 * * ?", "hello");
		ma = reusableCronService.save(ma);
	}
	
	@Test(expected = DuplicateKeyException.class)
	public void tCronDuplicate() throws ParseException {
		long num  = reusableCronRepository.count();
		assertThat(num, equalTo(0L));
		ReusableCron ma = new ReusableCron("0 30 6,12 * * ?", "hello");
		ma = reusableCronService.save(ma);
		
		ma = new ReusableCron("0 30 6,12 * * ?", "hello");
		ma = reusableCronService.save(ma);

	}


}
