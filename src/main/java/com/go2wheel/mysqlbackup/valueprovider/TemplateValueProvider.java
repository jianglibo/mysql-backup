package com.go2wheel.mysqlbackup.valueprovider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.TemplateIndicator;

public class TemplateValueProvider implements ValueProvider {

	@Value("${spring.freemarker.template-loader-path}")
	private String freemarkLoadPath;

	@Value("${spring.thymeleaf.prefix}")
	private String thymeleafPrefix;

	private Path freemarkerPath;
	private Path thymeleafPath;

	@PostConstruct
	public void post() {
		try {
			freemarkerPath = Paths.get(freemarkLoadPath.split(":", 2)[1]);
			thymeleafPath = Paths.get(thymeleafPrefix.split(":", 2)[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
		TemplateIndicator sv = parameter.getParameterAnnotation(TemplateIndicator.class);
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

		List<String> ls = new ArrayList<>();

		if (freemarkerPath != null) {
			try {
				ls = Files.list(freemarkerPath).map(p -> p.getFileName()).map(Object::toString)
						.filter(s -> s.startsWith(input) && s.endsWith(".ftl")).collect(Collectors.toList());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		List<String> ls1 = new ArrayList<>();

		if (thymeleafPath != null) {
			try {
				ls1 = Files.list(thymeleafPath).map(p -> p.getFileName()).map(Object::toString)
						.filter(s -> s.startsWith(input) && s.endsWith(".html")).collect(Collectors.toList());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ls.addAll(ls1);

		return ls.stream().map(CompletionProposal::new).collect(Collectors.toList());
	}
}
