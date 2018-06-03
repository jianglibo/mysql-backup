package com.go2wheel.mysqlbackup.valueprovider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.CronStringIndicator;
import com.go2wheel.mysqlbackup.annotation.ObjectFieldIndicator;
import com.go2wheel.mysqlbackup.annotation.ShowPossibleValue;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.service.ReuseableCronService;
import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;

public class ObjectFieldValueProvider implements ValueProvider {

	@Autowired
	private ReuseableCronService reusableCronService;

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		return isCronField(completionContext, parameter);
	}

	private boolean isCronField(CompletionContext completionContext, MethodParameter parameter) {
		ObjectFieldIndicator sv = parameter.getParameterAnnotation(ObjectFieldIndicator.class);
		if (sv == null)return false;
		Method md = parameter.getMethod();
		Optional<Parameter> pvOp = Arrays.stream(md.getParameters()).filter(p -> p.getAnnotation(ShowPossibleValue.class) != null).findAny();
		if (pvOp.isPresent()) {
			String pn = "--" + pvOp.get().getName();
			List<String> words = completionContext.getWords();
			int len = words.size();
			for(int i=0; i< len; i++) {
				String w = words.get(i);
				if (w.equals(pn) && i + 1 < len) {
					String fn = words.get(i + 1);
					Optional<Field> fd = ObjectUtil.getField(sv.objectClass(), fn);
					if (fd.isPresent()) {
						CronExpressionConstraint ci = fd.get().getAnnotation(CronExpressionConstraint.class); 
						return ci != null;
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-")) {
			return new ArrayList<>();
		}
		if (isCronField(completionContext, parameter)) {
			List<ReusableCron> crons = reusableCronService.findAll();
			if (crons.size() == 1) {
				return crons.stream().map(o -> o.getExpression())
						.map(CompletionProposal::new).collect(Collectors.toList());
			} else {
				return crons.stream().map(o -> o.toListRepresentation())
						.map(CompletionProposal::new).collect(Collectors.toList());
			}
		}
		return new ArrayList<>();
	}

}
