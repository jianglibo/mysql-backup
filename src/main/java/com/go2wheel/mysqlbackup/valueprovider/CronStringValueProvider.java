package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.CronString;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.service.ReuseableCronService;
import com.go2wheel.mysqlbackup.util.StringUtil;

public class CronStringValueProvider implements ValueProvider {

	@Autowired
	private ReuseableCronService reusableCronService;

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		CronString sv = parameter.getParameterAnnotation(CronString.class);
		return sv != null;
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-")) {
			return new ArrayList<>();
		}
		if (input == null || input.isEmpty()) {
			List<ReusableCron> crons = reusableCronService.findAll();
			if (crons.size() == 1) {
				return crons.stream().map(o -> o.getExpression())
						.map(CompletionProposal::new).collect(Collectors.toList());
			} else {
				return crons.stream().map(o -> o.toListRepresentation())
						.map(CompletionProposal::new).collect(Collectors.toList());
			}
		}
		int id = StringUtil.parseInt(input);
		if (id > 0) {
			return Stream.of(reusableCronService.findById(id)).filter(Objects::nonNull).map(o -> o.getExpression())
					.map(CompletionProposal::new).collect(Collectors.toList());
		}
		return new ArrayList<>();

	}

}
