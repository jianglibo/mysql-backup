package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.DbTableName;
import com.go2wheel.mysqlbackup.dbservice.TableDiscovery;

public class DbTableNameProvider implements ValueProvider {

	@Autowired
	private TableDiscovery tableDiscovery;
	
	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		DbTableName sv = parameter.getParameterAnnotation(DbTableName.class);
		return sv != null;
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
			String[] hints) {

		String input = completionContext.currentWordUpToCursor();
		// The input may be -- or --xxx. Because it's might a positional parameter.
		if (input.startsWith("-")) {
			return new ArrayList<>();
		}
		List<String> tableNames = tableDiscovery.getTableNames(input);
		return tableNames.stream().map(CompletionProposal::new).collect(Collectors.toList());
	}

}
