package com.go2wheel.mysqlbackup.valueprovider;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.ObjectFieldIndicator;
import com.go2wheel.mysqlbackup.annotation.OstypeIndicator;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.service.ReuseableCronDbService;
import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;

public class ObjectFieldValueProvider implements ValueProvider {

	@Autowired
	private ReuseableCronDbService reusableCronDbService;

	@Autowired
	private SharedValueProviderMethods svpm;

	private static List<Class<? extends Annotation>> al = new ArrayList<>();

	static {
		al.add(CronExpressionConstraint.class);
		al.add(OstypeIndicator.class);
	}

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		// return svpm.filedHasAnnotation(completionContext, parameter, al) != null;
		return parameter.getParameterAnnotation(ObjectFieldIndicator.class) != null;

	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-")) {
			return new ArrayList<>();
		}

		Object an = svpm.filedHasAnnotation(completionContext, parameter, al);

		if (an != null) {
			if (an instanceof CronExpressionConstraint) {
				List<ReusableCron> crons = reusableCronDbService.findAll();
				if (crons.size() == 1) {
					return crons.stream().map(o -> o.getExpression()).map(CompletionProposal::new)
							.collect(Collectors.toList());
				} else {
					return crons.stream().map(o -> o.toListRepresentation()).map(CompletionProposal::new)
							.collect(Collectors.toList());
				}
			} else if (an instanceof OstypeIndicator) {
				return svpm.getOstypeProposals(input);
			}
		}
		return svpm.getOriginValue(completionContext, parameter, input);
	}

}
