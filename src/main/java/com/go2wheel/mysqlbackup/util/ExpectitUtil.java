package com.go2wheel.mysqlbackup.util;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.jcraft.jsch.Channel;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;

public class ExpectitUtil {
	
	public static void comsumeInputs(Expect expect, Matcher<Result> matcher) {
		try {
			expect.withTimeout(200, TimeUnit.MILLISECONDS).expect(matcher).getBefore();
		} catch (Exception e) {
		}
	}
	
	public static ExpectBuilder getExpectBuilder(Channel channel, boolean echo) throws IOException {
		ExpectBuilder eb = new ExpectBuilder()
				.withOutput(channel.getOutputStream())
				.withInputs(channel.getInputStream(), channel.getExtInputStream())
				.withExceptionOnFailure();
		
		if (echo) {
			eb.withEchoOutput(System.out)
			.withEchoInput(System.err);
		}
		
		return eb;
	}

}
