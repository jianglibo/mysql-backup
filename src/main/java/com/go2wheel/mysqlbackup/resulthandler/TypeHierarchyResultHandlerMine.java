package com.go2wheel.mysqlbackup.resulthandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.shell.ResultHandler;

public class TypeHierarchyResultHandlerMine implements ResultHandler<Object> {

	private Map<Class<?>, ResultHandler<?>> resultHandlers = new HashMap<>();

	@SuppressWarnings("unchecked")
	public void handleResult(Object result) {
		if (result == null) { // void methods
			return;
		}
		Class<?> clazz = result.getClass();
		@SuppressWarnings("rawtypes")
		ResultHandler handler = getResultHandler(clazz);
		handler.handleResult(result);
	}

	@SuppressWarnings("rawtypes")
	private ResultHandler getResultHandler(Class<?> clazz) {
		ResultHandler handler = resultHandlers.get(clazz);
		if (handler != null) {
			return handler;
		}
		else {
			for (Class type : clazz.getInterfaces()) {
				handler = getResultHandler(type);
				if (handler != null) {
					return handler;
				}
			}
			return clazz.getSuperclass() != null ? getResultHandler(clazz.getSuperclass()) : null;
		}
	}

	@Autowired
	public void setResultHandlers(Set<ResultHandler<?>> resultHandlers) {
		for (ResultHandler<?> resultHandler : resultHandlers) {
			ResolvableType type = ResolvableType.forInstance(resultHandler).as(ResultHandler.class);
			registerHandler(type.resolveGeneric(0), resultHandler);
		}
	}

	private void registerHandler(Class<?> type, ResultHandler<?> resultHandler) {
		ResultHandler<?> previous = this.resultHandlers.put(type, resultHandler);
		if (previous != null) {
			throw new IllegalArgumentException(String.format("Multiple ResultHandlers configured for %s: both %s and %s", type, previous, resultHandler));
		}
	}

}
