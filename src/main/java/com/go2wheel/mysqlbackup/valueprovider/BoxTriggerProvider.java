package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.job.SchedulerService;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;

public class BoxTriggerProvider implements ValueProvider {

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private SchedulerService schedulerService;
	
	private Logger logger = LoggerFactory.getLogger(getClass());


	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		String s = parameter.getMethod().getName();
		return s.equals("schedulerTriggerDelete");
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-") || applicationState.getCurrentServer() == null) {
			return new ArrayList<>();
		}
		Server box = applicationState.getCurrentServer();
		
		switch (parameter.getParameterName()) {
		case "triggerKey":
			try {
				return schedulerService.getServerTriggers(box).stream().map(tg -> tg.getKey().toString()).map(CompletionProposal::new).collect(Collectors.toList());
			} catch (SchedulerException e) {
				ExceptionUtil.logErrorException(logger, e);
			}
		default:
			break;
		}
		return new ArrayList<>();
	}

}
