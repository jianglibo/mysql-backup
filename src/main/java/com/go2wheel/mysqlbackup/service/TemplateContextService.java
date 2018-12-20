package com.go2wheel.mysqlbackup.service;

import com.go2wheel.mysqlbackup.mail.ServerContext;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.value.Server;
import com.go2wheel.mysqlbackup.value.ServerGrp;
import com.go2wheel.mysqlbackup.value.Subscribe;
import com.go2wheel.mysqlbackup.value.UserAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class TemplateContextService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private UserGroupLoader userGroupLoader;

  /**
   * Create context for presentation layer.
   * @param subscribe subscribe
   * @return root context.
   * @throws ExecutionException
   */
  public ServerGroupContext createMailerContext(Subscribe subscribe) throws ExecutionException {
    ServerGrp sg = userGroupLoader.getGroupByName(subscribe.getGroupname());
    UserAccount ua = userGroupLoader.getUserByName(subscribe.getUsername());

    List<Server> servers = sg.getServers();

    List<ServerContext> oscs = new ArrayList<>();

    for (Server server : servers) {
      ServerContext osc = prepareServerContext(server);
      oscs.add(osc);
    }
    return new ServerGroupContext(oscs, ua, sg);
  }

  public ServerContext prepareServerContext(Server server) {
    ServerContext osc = new ServerContext(server);
    return osc;
  }

}
