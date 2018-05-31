package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.job.SchedulerService;
import com.go2wheel.mysqlbackup.value.MysqlInstanceYml;

public class MysqlDescriptionProvider implements ValueProvider {

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private SchedulerService schedulerTaskFacade;


	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		String s = parameter.getMethod().getName();
		return s.equals("mysqlDescriptionUpdate");
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-") || !applicationState.currentBoxOptional().isPresent()) {
			return new ArrayList<>();
		}
		MysqlInstanceYml mysqlq = applicationState.currentBoxOptional().get().getMysqlInstance();
		switch (parameter.getParameterName()) {
		case "username":
			return Arrays.asList(new CompletionProposal(mysqlq.getUsername()));
		case "password":
			return Arrays.asList(new CompletionProposal(mysqlq.getPassword()));
		case "port":
			return Arrays.asList(new CompletionProposal(mysqlq.getPort() + ""));
		case "flushLogCron":
			return Arrays.asList(new CompletionProposal(mysqlq.getFlushLogCron()));
		default:
			break;
		}

		return new ArrayList<>();
	}

}
