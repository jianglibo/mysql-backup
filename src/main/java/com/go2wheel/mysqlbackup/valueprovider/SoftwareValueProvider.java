package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.MetaAnno;
import com.go2wheel.mysqlbackup.dbservice.SoftwareDbService;
import com.go2wheel.mysqlbackup.model.Software;

public class SoftwareValueProvider  implements ValueProvider {
	
	@Autowired
	private SoftwareDbService softwareDbService;

    @Override
    public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
        return parameter.getParameterType().equals(Software.class);
    }

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {

        String input = completionContext.currentWordUpToCursor();
        // The input may be -- or --xxx. Because it's might a positional parameter.
        if (input.startsWith("-")) {
        	return new ArrayList<>();
        }
        
        MetaAnno ma = parameter.getMethodAnnotation(MetaAnno.class);

        List<Software> softwares;
        if (ma != null && !ma.value().trim().isEmpty()) {
        	softwares = softwareDbService.findByName(ma.value());
        } else {
        	softwares = softwareDbService.findAll();
        }
        
        return softwares.stream().map(sv -> sv.toListRepresentation()).map(h -> new CompletionProposal(h)).collect(Collectors.toList());
    }
    
}
