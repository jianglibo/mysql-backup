package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.job.SchedulerService;
import com.go2wheel.mysqlbackup.value.Box;

public class MysqlDescriptionProvider implements ValueProvider {

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private SchedulerService schedulerTaskFacade;

	private static final Set<String> paramterNames = new HashSet<>();

	static {
		paramterNames.add("host");
		paramterNames.add("triggerName");
	}
	
	public MysqlDescriptionProvider() {
		System.out.println("OK");
	}

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		String s = parameter.getMethod().getName();
		return s.equals("mysqlDescriptionCreateOrUpdate");
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-") || !applicationState.currentBox().isPresent()) {
			return new ArrayList<>();
		}
		Box box = applicationState.currentBox().get();
		switch (parameter.getParameterName()) {
		case "username":
			return Arrays.asList(new CompletionProposal(box.getUsername()));
		case "password":
			return Arrays.asList(new CompletionProposal(box.getPassword()));
		case "port":
			return Arrays.asList(new CompletionProposal(box.getPort() + ""));
		case "flushLogCron":
			return Arrays.asList(new CompletionProposal(box.getPassword()));
		default:
			break;
		}

		return new ArrayList<>();
	}

}
