package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.annotation.ShowPossibleValue;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;

public class ServerUpdateParameterProvider implements ValueProvider {

	@Autowired
	private ApplicationState applicationState;

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		String s = parameter.getMethod().getName();
		return s.equals("serverUpdate");
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-") || !applicationState.currentServerOptional().isPresent()) {
			return new ArrayList<>();
		}
		Server server = applicationState.currentServerOptional().get();
		String tip;
		switch (parameter.getParameterName()) {
		case "name":
			return Arrays.asList(new CompletionProposal(server.getName()));
		case "username":
			return Arrays.asList(new CompletionProposal(server.getUsername()));
		case "password":
			if (!StringUtil.hasAnyNonBlankWord(server.getPassword())) {
				tip = Box.NO_PASSWORD;
			} else {
				tip = server.getPassword();
			}
			return Arrays.asList(new CompletionProposal(tip));
		case "port":
			return Arrays.asList(new CompletionProposal(server.getPort() + ""));
		case "serverRole":
			if (StringUtil.hasAnyNonBlankWord(server.getServerRole())) {
				return Arrays.asList(new CompletionProposal(server.getServerRole()));
			} else {
				ShowPossibleValue sv = parameter.getParameterAnnotation(ShowPossibleValue.class);
				if (sv != null) {
					return Stream.of(sv.value()).map(CompletionProposal::new).collect(Collectors.toList());
				}
			}
		default:
			break;
		}

		return new ArrayList<>();
	}

}
