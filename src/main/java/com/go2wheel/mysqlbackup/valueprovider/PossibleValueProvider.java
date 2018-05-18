package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.ShowPossibleValue;

public class PossibleValueProvider implements ValueProvider {

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		ShowPossibleValue sv = parameter.getParameterAnnotation(ShowPossibleValue.class);
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
		ShowPossibleValue so = parameter.getParameterAnnotation(ShowPossibleValue.class);
		return Stream.of(so.value()).map(CompletionProposal::new).collect(Collectors.toList());
	}

}
