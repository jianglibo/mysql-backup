package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.service.ConfigFileLoader;
import com.go2wheel.mysqlbackup.value.ConfigFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

public class ConfigFileCmdProvider implements ValueProvider {

	@Autowired
	private ConfigFileLoader configFileLoader;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		String mn = parameter.getMethod().getName();
		String pn = parameter.getParameterName();
		return "psCmdKey".equals(pn);
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		List<String> words = completionContext.getWords();
		// The input may be -- o --xxx. Because it's might a positional parameter.
		if (input.startsWith("-") || (words.size() < 3)) {
			return new ArrayList<>();
		}

		ConfigFile cf = configFileLoader.getOne(words.get(1));
		if (cf != null) {
			List<String> matches = cf.getCrons().keySet().stream().filter(jn -> {
				return jn.startsWith(input);
			}).collect(Collectors.toList());

			return matches.stream().map(CompletionProposal::new).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}
