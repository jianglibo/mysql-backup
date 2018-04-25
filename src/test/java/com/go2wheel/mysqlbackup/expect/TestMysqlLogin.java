package com.go2wheel.mysqlbackup.expect;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;
import static net.sf.expectit.matcher.Matchers.anyString;
import static net.sf.expectit.matcher.Matchers.exact;

import java.io.IOException;
import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.filter.Filter;
import net.sf.expectit.filter.FilterAdapter;

public class TestMysqlLogin extends SshBaseFort {

	@Test
	public void tLogin() throws IOException, JSchException {
		Channel channel = sshSession.openChannel("shell");
		channel.connect();

		// @formatter:off
		Expect expect = new ExpectBuilder()
				.withOutput(channel.getOutputStream())
				.withInputs(channel.getInputStream(), channel.getExtInputStream())
				.withEchoOutput(System.out)
				.withEchoInput(System.err)
				.withExceptionOnFailure().build();
		try {
			expect.sendLine("mysql -uroot -p");
			Result result = expect.expect(contains("password:"));
			System.out.println(result);
			expect.sendLine("123456");
			result = expect.expect(regexp(".*mysql>\\s*$"));
			System.out.println(result);
//			expect.send("N");
//			expect.expect(regexp(": $"));
		} finally {
			expect.close();
			channel.disconnect();
		}

	}

}
