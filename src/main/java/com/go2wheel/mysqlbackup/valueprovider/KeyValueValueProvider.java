package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.dbservice.KeyValueDbService;
import com.go2wheel.mysqlbackup.model.KeyValue;

public class KeyValueValueProvider  implements ValueProvider {
	
	@Autowired
	private KeyValueDbService keyValueDbService;

    @Override
    public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
        return parameter.getParameterType().equals(KeyValue.class);
    }

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {

        String input = completionContext.currentWordUpToCursor();
        // The input may be -- or --xxx. Because it's might a positional parameter.
        if (input.startsWith("-")) {
        	return new ArrayList<>();
        }

        List<KeyValue> kvlist = keyValueDbService.findByKeyPrefix(input); 
        return kvlist.stream().map(sv -> sv.getItemKey()).map(h -> new CompletionProposal(h)).collect(Collectors.toList());
    }
    
}
