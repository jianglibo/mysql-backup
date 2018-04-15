package com.go2wheel.mysqlbackup.valueprovider;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

public class FileValueProviderMine  implements ValueProvider {

    @Override
    public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
        return parameter.getParameterType().equals(File.class);
    }

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {

        String input = completionContext.currentWordUpToCursor();
        // The input may be -- or --xxx. Because it's might a positional parameter.
        if (input.startsWith("-")) {
        	return new ArrayList<>();
        }
        input = input.replaceAll("\\\\", "/");
        
        if ("..".equals(input)) {
        	input = "../";
        }
        
        int lastSlash = input.lastIndexOf("/");
        Path dir = lastSlash > -1 ? Paths.get(input.substring(0, lastSlash+1)) : Paths.get("");
        
        if (!Files.exists(dir)) {
        	return new ArrayList<>();
        }
        String prefix = input.substring(lastSlash + 1, input.length());

        try {
            return Files.find(dir, 1, (p, a) -> p.getFileName() != null && p.getFileName().toString().startsWith(prefix), FOLLOW_LINKS)
                    .map(p -> new CompletionProposal(p.toString().replaceAll("\\\\", "/")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }
    
}
