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
import com.go2wheel.mysqlbackup.value.BorgBackupDescription;

public class BorgDescriptionProvider implements ValueProvider {

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private SchedulerService schedulerTaskFacade;


	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		String s = parameter.getMethod().getName();
		return s.equals("borgDescriptionUpdate");
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-") || !applicationState.currentBox().isPresent()) {
			return new ArrayList<>();
		}
		BorgBackupDescription bbd = applicationState.currentBox().get().getBorgBackup();
		switch (parameter.getParameterName()) {
		case "repo":
			return Arrays.asList(new CompletionProposal(bbd.getRepo()));
		case "archiveFormat":
			return Arrays.asList(new CompletionProposal(bbd.getArchiveFormat()));
		case "archiveNamePrefix":
			return Arrays.asList(new CompletionProposal(bbd.getArchiveNamePrefix()));
		case "archiveCron":
			return Arrays.asList(new CompletionProposal(bbd.getArchiveCron()));
		case "pruneCron":
			return Arrays.asList(new CompletionProposal(bbd.getPruneCron()));
		default:
			break;
		}

		return new ArrayList<>();
	}

}
