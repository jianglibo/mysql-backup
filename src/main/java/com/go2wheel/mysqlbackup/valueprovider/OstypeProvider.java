package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.OstypeIndicator;

public class OstypeProvider implements ValueProvider {

	@Autowired
	private SharedValueProviderMethods svpm;
	
	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		OstypeIndicator sv = parameter.getParameterAnnotation(OstypeIndicator.class);
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
		return svpm.getOstypeProposals(input);
	}

}
