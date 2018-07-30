package com.go2wheel.mysqlbackup.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.jooq.impl.TableImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.stereotype.Service;

import com.google.common.base.CaseFormat;

@Service
public class TableDiscovery {

	private Map<String, Object> tablemap = new HashMap<>();

	@PostConstruct
	public void post() {
		final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
				false);
		// add include filters which matches all the classes (or use your own)
		provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
		// get matching classes defined in the package
		final Set<BeanDefinition> classes = provider
				.findCandidateComponents("com.go2wheel.mysqlbackup.jooqschema.tables");

		// this is how you can load the class type from BeanDefinition instance
		for (BeanDefinition bean : classes) {
//			Class<? extends TableImpl<?>> clazz;
			Class<?> clazz;
			try {
				clazz = Class.forName(bean.getBeanClassName());
				if (TableImpl.class.isAssignableFrom(clazz)) {
					String s = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(clazz.getSimpleName());
					tablemap.put(s, clazz.newInstance());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends TableImpl<?>> T getTable(String tableName) {
		return (T) tablemap.get(tableName.toLowerCase());
	}

	public List<String> getTableNames(String input) {
		String u = input.toLowerCase();
		return tablemap.keySet().stream().filter(n -> n.contains(u)).collect(Collectors.toList());
	}

}
