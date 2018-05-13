package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
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

public class ServerInfoProvider implements ValueProvider {

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private SchedulerService schedulerTaskFacade;

	private static final Set<String> paramterNames = new HashSet<>();

	static {
		paramterNames.add("host");
		paramterNames.add("triggerName");
	}

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		return ServerInfoProvider.paramterNames.contains(parameter.getParameterName());
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-")) {
			return new ArrayList<>();
		}
		Optional<Box> cb = applicationState.currentBox();
		switch (parameter.getParameterName()) {
		case "host":
			return applicationState.getServers().stream().map(box -> box.getHost()).filter(h -> h.contains(input))
					.map(CompletionProposal::new).collect(Collectors.toList());
		case "triggerName":
			if (cb.isPresent()) {
				try {
					schedulerTaskFacade.getBoxTriggers(cb.get()).stream().map(t -> t.getKey())
							.map(tk -> tk.getGroup() + "." + tk.getName()).map(CompletionProposal::new)
							.collect(Collectors.toList());
				} catch (SchedulerException e) {
					e.printStackTrace();
				}
			}
		default:
			break;
		}

		return new ArrayList<>();
	}

}
