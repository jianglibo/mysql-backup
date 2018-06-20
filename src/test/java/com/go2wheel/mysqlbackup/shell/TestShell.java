package com.go2wheel.mysqlbackup.shell;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.jline.terminal.Terminal;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.MethodTarget;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.Shell;
import org.springframework.shell.result.TypeHierarchyResultHandler;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.resulthandler.TypeHierarchyResultHandlerMine;

public class TestShell extends SpringBaseFort {
	
	@Autowired
	private Shell shell;
	
	@Autowired @Lazy
	public void setTerminal(Terminal terminal) {
		int i = terminal.getWidth();
		UtilForTe.printme(i);
	}

	
	@Autowired
	@Qualifier("main")
	private ResultHandler<?> resultHandler;
	
	@Test
	public void tShell() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		UtilForTe.printme(shell.listCommands());
		assertTrue("should contain clear command", shell.listCommands().containsKey("clear"));
		MethodTarget mt = shell.listCommands().get("clear");
		mt.getMethod().invoke(mt.getBean());
		UtilForTe.printme(mt.getBean());
		assertTrue("should be instance of TypeHierarchyResultHandler", resultHandler instanceof TypeHierarchyResultHandler);
		
	}

}
