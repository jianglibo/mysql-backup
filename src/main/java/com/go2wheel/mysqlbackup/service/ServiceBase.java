package com.go2wheel.mysqlbackup.service;

import java.util.List;

import javax.validation.Valid;

import org.jooq.Condition;
import org.jooq.SortField;
import org.jooq.UpdatableRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.repository.RepositoryBase;

public abstract class ServiceBase<R extends UpdatableRecord<R>, P extends BaseModel> implements ApplicationEventPublisherAware {
	
	private ApplicationEventPublisher applicationEventPublisher;
	
	protected RepositoryBase<R, P> repo;
	
	public ServiceBase(RepositoryBase<R, P> repo) {
		this.repo = repo;
	}
	
	public P save(@Valid P pojo) {
		Integer id = pojo.getId();
		P saved;
		if (id == null || id == 0) {
			saved = repo.insertAndReturn(pojo);
			applicationEventPublisher.publishEvent(new ModelCreatedEvent<P>(saved));
		} else {
			P beforeSave = repo.findById(id);
			saved = repo.insertAndReturn(pojo);
			applicationEventPublisher.publishEvent(new ModelChangedEvent<P>(beforeSave, saved));
		}
		return saved;
	}
	
	public List<P> findAll() {
		return repo.findAll();
	}

	public List<P> findAll(int offset, int limit) {
		return repo.findAll(offset, limit);
	}
	
	public List<P> findAll(SortField<?> sf, int offset, int limit) {
		return repo.findAll(sf, offset, limit);
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
	
	public List<P> getRecentItems(Server server, int number) {
		return repo.getRecentItems(server, number);
	}

	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
	public List<P> findAll(Condition eq, int offset, int limit) {
		return repo.findAll(eq, offset, limit);
	}

}
