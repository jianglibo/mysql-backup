package com.go2wheel.mysqlbackup.event;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import com.go2wheel.mysqlbackup.model.BaseModel;

public class ModelChangedEvent<T extends BaseModel> implements ResolvableTypeProvider {
	
	private T before;
	
	private T after;
	
    public ModelChangedEvent(T before, T after) {
        this.before = before;
        this.after = after;
    }

	@Override
	public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(),
                ResolvableType.forInstance(before));
	}

	public T getBefore() {
		return before;
	}

	public void setBefore(T before) {
		this.before = before;
	}

	public T getAfter() {
		return after;
	}

	public void setAfter(T after) {
		this.after = after;
	}
	
}
