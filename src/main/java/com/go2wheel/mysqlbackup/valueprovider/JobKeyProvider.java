package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.job.SchedulerService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;

public class JobKeyProvider implements ValueProvider {

	@Autowired
	private SchedulerService schedulerService;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		return parameter.getParameterType().equals(JobKey.class);
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-")) {
			return new ArrayList<>();
		}
		try {
			return schedulerService.getAllJobKeys().stream().map(StringUtil::formatJobkey).filter(jn -> jn.startsWith(input))
					.map(CompletionProposal::new).collect(Collectors.toList());
		} catch (SchedulerException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
		return new ArrayList<>();
	}

}
