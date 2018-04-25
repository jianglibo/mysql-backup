package com.go2wheel.mysqlbackup.expect;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

import java.io.IOException;

import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;

public class TestMysqlLogin extends SshBaseFort {

	
//	@Test
//	public void tLogin() throws IOException {
//		final Session session = sshSession.startSession();
//		try {
//	        Expect expect = new ExpectBuilder()
//	                .withOutput(session.getOutputStream())
//	                .withInputs(session.getInputStream())
//	                .withEchoOutput(System.out)
//	                .withEchoInput(System.err)
//	                .withExceptionOnFailure()
//	                .build();
//	        try {
//	            expect.sendLine("mysql -uroot -p");
//	            String ipAddress = expect.expect(regexp("Trying (.*)\\.\\.\\.")).group(1);
//	            System.out.println("Captured IP: " + ipAddress);
//	            expect.expect(contains("login:"));
//	            expect.sendLine("new");
//	            expect.expect(contains("(Y/N)"));
//	            expect.send("N");
//	            expect.expect(regexp(": $"));
//	        } finally {
//	            expect.close();
//	            session.close();
//	        }
//		} finally {
//			session.close();
//		}
//
//	}

}
