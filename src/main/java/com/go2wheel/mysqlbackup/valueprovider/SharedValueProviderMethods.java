package com.go2wheel.mysqlbackup.valueprovider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.annotation.ObjectFieldIndicator;
import com.go2wheel.mysqlbackup.annotation.ShowPossibleValue;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.service.ReuseableCronDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.ObjectUtil;

@Service
public class SharedValueProviderMethods {
	
	private static Set<String> predefines = new HashSet<>(Arrays.asList("linux_centos", "linux_centos_7" ,"win", "win_10", "win_2008" ,"win_2012"));
	
	@Autowired
	private ReuseableCronDbService reusableCronDbService;
	
	@Autowired
	private ServerDbService serverDbService;
	
	
	public List<CompletionProposal> getOstypeProposals(String input) {
		Set<String> oses = new HashSet<>(serverDbService.findDistinctOsType(input));
		oses.addAll(predefines);
		return oses.stream().map(CompletionProposal::new).collect(Collectors.toList());
	}

	public Object filedHasAnnotation(CompletionContext completionContext, MethodParameter parameter, List<Class<? extends Annotation>> al) {
		ObjectFieldIndicator sv = parameter.getParameterAnnotation(ObjectFieldIndicator.class);
		if (sv == null)return null;
		Method md = parameter.getMethod();
		/**
		 * Because ObjectFieldValueProvider usually has a @see ShowPossibleValue annoation which list all of possible filed names.
		 * So by getting the value of --field which already entered we can get the field name of the target object, then get the field information by java reflecting. 
		 */
		Optional<Parameter> pvOp = Arrays.stream(md.getParameters()).filter(p -> p.getAnnotation(ShowPossibleValue.class) != null).findAny();
		if (pvOp.isPresent()) {
			String pn = "--" + pvOp.get().getName(); // --field os
			List<String> words = completionContext.getWords();
			int len = words.size();
			for(int i=0; i< len; i++) {
				String w = words.get(i);
				if (w.equals(pn) && i + 1 < len) {
					String fn = words.get(i + 1);
					Optional<Field> fd = ObjectUtil.getField(sv.objectClass(), fn);
					if (fd.isPresent()) {
						for(Class<? extends Annotation> t : al) {
							Object an = fd.get().getAnnotation(t);
							if (an != null) {
								return an;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public List<CompletionProposal> getCronProposals() {
		List<ReusableCron> crons = reusableCronDbService.findAll();
		return crons.stream().map(o -> o.toListRepresentation())
				.map(CompletionProposal::new).collect(Collectors.toList());
	}

}
