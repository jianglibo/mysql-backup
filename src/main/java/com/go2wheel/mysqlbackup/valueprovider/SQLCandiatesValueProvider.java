package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.CandidatesFromSQL;

public class SQLCandiatesValueProvider implements ValueProvider {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		CandidatesFromSQL sv = parameter.getParameterAnnotation(CandidatesFromSQL.class);
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
		CandidatesFromSQL sv = parameter.getParameterAnnotation(CandidatesFromSQL.class);
		String sql = String.format(sv.value(), input);

		return jdbcTemplate.queryForList(sql).stream().flatMap(m -> m.values().stream())
				.map(Objects::toString).map(CompletionProposal::new).collect(Collectors.toList());
	}

}
