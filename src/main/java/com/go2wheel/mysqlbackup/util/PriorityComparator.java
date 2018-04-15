package com.go2wheel.mysqlbackup.util;

import java.util.Comparator;

import javax.annotation.Priority;

public class PriorityComparator implements Comparator<Object> {

	@Override
	public int compare(Object o1, Object o2) {
		Priority po1 = o1.getClass().getDeclaredAnnotation(Priority.class);
		Priority po2 = o2.getClass().getDeclaredAnnotation(Priority.class);
		int v1,v2;
		if (po1 == null) {
			v1 = Integer.MAX_VALUE - 1;
		} else {
			v1 = po1.value();
		}
		if (po2 == null) {
			v2 = Integer.MAX_VALUE - 1;
		} else {
			v2 = po2.value();
		}
		return v1 - v2;
	}

}
