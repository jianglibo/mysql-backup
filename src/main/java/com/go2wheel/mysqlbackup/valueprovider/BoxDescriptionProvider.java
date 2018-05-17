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
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;

public class BoxDescriptionProvider implements ValueProvider {

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private SchedulerService schedulerTaskFacade;


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
		if (input.startsWith("-") || !applicationState.currentBox().isPresent()) {
			return new ArrayList<>();
		}
		Box box = applicationState.currentBox().get();
		String tip;
		switch (parameter.getParameterName()) {
		case "username":
			return Arrays.asList(new CompletionProposal(box.getUsername()));
		case "password":
			if (!StringUtil.hasAnyNonBlankWord(box.getPassword())) {
				tip = Box.NO_PASSWORD;
			} else {
				tip = box.getPassword();
			}
			return Arrays.asList(new CompletionProposal(tip));
		case "port":
			return Arrays.asList(new CompletionProposal(box.getPort() + ""));
		case "boxRole":
			return Arrays.asList(new CompletionProposal(box.getRole()));
		default:
			break;
		}

		return new ArrayList<>();
	}

}
