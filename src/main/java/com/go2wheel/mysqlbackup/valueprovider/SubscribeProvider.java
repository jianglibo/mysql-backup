package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.service.ConfigFileLoader;
import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.value.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

public class SubscribeProvider implements ValueProvider {

	@Autowired
	private UserGroupLoader userGroupLoader;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		String mn = parameter.getMethod().getName();
		String pn = parameter.getParameterName();
		return parameter.getParameterType().equals(Subscribe.class);
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-")) {
			return new ArrayList<>();
		}
		List<String> matches = userGroupLoader.getAllSubscribes().stream().map(Object::toString).filter(jn -> {
			return jn.startsWith(input);
		}).collect(Collectors.toList());

		return matches.stream().map(CompletionProposal::new).collect(Collectors.toList());
	}
}
