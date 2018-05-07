package com.go2wheel.mysqlbackup.borg;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;
import com.jcraft.jsch.Session;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestBorgTaskFacadeSpring {
	
	@Autowired
	private BorgTaskFacade borgTaskFacade;
	
	@Autowired
	private ApplicationState appState;
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Test
	public void tArchive() throws RunRemoteCommandException, InterruptedException, IOException {
		Box box = UtilForTe.loadDemoBox();
		Session session = sshSessionFactory.getConnectedSession(box).get();
		borgTaskFacade.archive(session, box, "");
	}

}
