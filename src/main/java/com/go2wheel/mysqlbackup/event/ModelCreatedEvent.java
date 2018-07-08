package com.go2wheel.mysqlbackup.event;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import com.go2wheel.mysqlbackup.model.BaseModel;

public class ModelCreatedEvent<T extends BaseModel> implements ResolvableTypeProvider {
	
	private T model;
	
    public ModelCreatedEvent(T model) {
        this.model = model;
    }

	@Override
	public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(),
                ResolvableType.forInstance(model));
	}

	public T getModel() {
		return model;
	}

	public void setModel(T model) {
		this.model = model;
	}

}
