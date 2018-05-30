package com.go2wheel.mysqlbackup.service;

import java.util.List;

import javax.validation.Valid;

import org.jooq.UpdatableRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.repository.RepositoryBase;

public abstract class ServiceBase<R extends UpdatableRecord<R>, P extends BaseModel> implements ApplicationEventPublisherAware {
	
	private ApplicationEventPublisher applicationEventPublisher;
	
	protected RepositoryBase<R, P> repo;
	
	public ServiceBase(RepositoryBase<R, P> repo) {
		this.repo = repo;
	}
	
	@SuppressWarnings("unchecked")
	public P save(@Valid P pojo) {
		Integer id = pojo.getId();
		P origin = null;
		try {
			origin = (P) pojo.getClass().newInstance();
			BeanUtils.copyProperties(pojo, origin);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		P saved = repo.insertAndReturn(pojo);
		if (id == null || id == 0) {
			applicationEventPublisher.publishEvent(new ModelCreatedEvent<P>(saved));
		} else {
			applicationEventPublisher.publishEvent(new ModelChangedEvent<P>(origin, saved));
		}
		return saved;
	}
	
	public List<P> findAll() {
		return repo.findAll();
	}
	
	public P findById(Integer id) {
		return repo.findById(id);
	}
	
	public P findById(String id) {
		try {
			Integer i = Integer.parseInt(id);
			return findById(i);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public void delete(P pojo) {
		repo.delete(pojo);
		applicationEventPublisher.publishEvent(new ModelDeletedEvent<P>(pojo));
	}
	
	public long count() {
		return repo.count();
	}
	
	public void deleteAll() {
		findAll().forEach(item -> delete(item));
	}
	
	public List<P> getRecentItems(int number) {
		return repo.getRecentItems(number);
	}

	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

}
