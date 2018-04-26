package com.go2wheel.mysqlbackup.util;


import java.util.concurrent.TimeUnit;

import net.sf.expectit.Expect;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;

public class ExpectitUtil {
	
	public static void comsumeInputs(Expect expect, Matcher<Result> matcher) {
		try {
			expect.withTimeout(200, TimeUnit.MILLISECONDS).expect(matcher).getBefore();
		} catch (Exception e) {
		}
	}

}
