package com.go2wheel.mysqlbackup.convert;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.value.Subscribe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class IdToSubscribe implements Converter<String, Subscribe> {

	@Autowired
	private UserGroupLoader userGroupLoader;

	private Pattern ptn = Pattern.compile(".*?\\{(.*)\\}");

	// Subscribe{id=jlb,\ username=demouser,\ groupname=demogroup,\
	// template=ctx.html}

	@Override
	public Subscribe convert(String source) {
		Matcher m = ptn.matcher(source);
		if (m.matches()) {
			Optional<String[]> os = Stream.of(m.group(1).split(",\\s+")).map(s -> s.split("=", 2))
					.filter(ss -> ss.length == 2).filter(ss -> "id".equals(ss[0])).findAny();
			if (os.isPresent()) {
				return userGroupLoader.getSubscribeById(os.get()[1]);
			}
		}
		return null;
	}

}
