package com.go2wheel.mysqlbackup.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.util.StringUtil;
import com.google.common.base.Splitter;

public class ListStringToLinesEditor extends PropertyEditorSupport {
	
	private Splitter splitter = Splitter.on(Pattern.compile("\r?\n")).trimResults().omitEmptyStrings();

	@SuppressWarnings("unchecked")
	@Override
	public String getAsText() {
		List<String> ls = (List<String>) getValue();
		return ls == null ? "" : ls.stream().collect(Collectors.joining("\n"));
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (!StringUtil.hasAnyNonBlankWord(text)) {
			setValue(null);
		} else {
			List<String> ls = splitter.splitToList(text);
			setValue(ls);
		}
	}
}
