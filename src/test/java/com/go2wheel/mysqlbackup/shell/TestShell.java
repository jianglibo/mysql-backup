package com.go2wheel.mysqlbackup.shell;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.jline.terminal.Terminal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.MethodTarget;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.Shell;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.resulthandler.TypeHierarchyResultHandlerMine;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestShell {
	
	@Autowired
	private Shell shell;
	
	private Terminal terminal;
	
	@Autowired @Lazy
	public void setTerminal(Terminal terminal) {
		this.terminal = terminal;
		int i = terminal.getWidth();
		UtilForTe.printme(i);
	}

	
	@Autowired
	@Qualifier("main")
	private ResultHandler resultHandler;
	
	@Test
	public void tShell() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		UtilForTe.printme(shell.listCommands());
		assertTrue("should contain clear command", shell.listCommands().containsKey("clear"));
		MethodTarget mt = shell.listCommands().get("clear");
		mt.getMethod().invoke(mt.getBean());
		UtilForTe.printme(mt.getBean());
		assertTrue("should be instance of TypeHierarchyResultHandler", resultHandler instanceof TypeHierarchyResultHandlerMine);
		
	}

}
