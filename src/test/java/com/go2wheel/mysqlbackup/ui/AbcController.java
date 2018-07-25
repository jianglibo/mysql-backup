package com.go2wheel.mysqlbackup.ui;

import java.util.stream.IntStream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AbcController {
	
	
	@PostMapping("/test/abc")
	@ResponseBody
	public String testFormUrlEncoded(@RequestParam("a") int[] a) {
		return IntStream.of(a).sum() + "";
	}

}
