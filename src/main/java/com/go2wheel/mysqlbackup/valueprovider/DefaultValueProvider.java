package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.ShowDefaultValue;

public class DefaultValueProvider implements ValueProvider {

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		ShowDefaultValue sv = parameter.getParameterAnnotation(ShowDefaultValue.class);
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
		
		ShellOption so = parameter.getParameterAnnotation(ShellOption.class);
		if (so != null && !ShellOption.NONE.equals(so.defaultValue())) {
			return Arrays.asList(new CompletionProposal(so.defaultValue()));
		}
		return new ArrayList<>();
	}

}
