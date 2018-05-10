package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.exception.EntityNotFoundException;
import com.go2wheel.mysqlbackup.model.MailAddress;

public interface MailAddressRepository {
    public MailAddress add(MailAddress mailAddressEntry);
    
    public MailAddress delete(int id) throws EntityNotFoundException;
 
    public List<MailAddress> findAll();
 
    public MailAddress findById(int id) throws EntityNotFoundException;
 
    public MailAddress update(MailAddress MailAddressEntry);
}
