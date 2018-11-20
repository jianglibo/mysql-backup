package com.go2wheel.mysqlbackup.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import com.go2wheel.mysqlbackup.annotation.SetServerOnly;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.model.Server;

public class ServerValueProvider  implements ValueProvider {
	
	@Autowired
	private ServerDbService serverDbService;

    @Override
    public boolean supports(MethodParameter parameter, CompletionContext completionContext) {
        return parameter.getParameterType().equals(Server.class);
    }

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {

        String input = completionContext.currentWordUpToCursor();
        // The input may be -- or --xxx. Because it's might a positional parameter.
        if (input.startsWith("-")) {
        	return new ArrayList<>();
        }

		SetServerOnly sso = parameter.getParameterAnnotation(SetServerOnly.class);
        List<Server> servers; 
        if (sso == null) {
        	servers = serverDbService.findLikeHost(input);
        } else {
        	servers = serverDbService.findLikeHostAndRoleIs(input, "GET");
        }
        return servers.stream().map(sv -> sv.toListRepresentation()).map(h -> new CompletionProposal(h)).collect(Collectors.toList());
    }
    
}
