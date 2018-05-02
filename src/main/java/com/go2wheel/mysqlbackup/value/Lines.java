package com.go2wheel.mysqlbackup.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Lines {
	
	private List<String> lines;
	
	public Lines(List<String> lines) {
		this.lines = lines;
	}
	
	public Optional<String> findMatchAndReturnNextLine(String ptn) {
		List<String> result = findMatchAndReturnNextLines(Pattern.compile(ptn), 1);
		
		if(result.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(result.get(0));
	}
	
	public List<String> findMatchAndReturnNextLines(Pattern ptn, int howmuch) {
		List<String> results = new ArrayList<>();
		int totalLines = lines.size();
		int foundPos = -1;
		for (int i = 0; i < totalLines; i++) {
			String line = lines.get(i);
			if (ptn.matcher(line).matches()) {
				foundPos = i;
				break;
			}
		}
		if (foundPos == -1) {
			return results;
		} else {
			int top = foundPos + howmuch + 1;
			if (top > totalLines){
				top = totalLines;
			}
			return lines.subList(foundPos + 1, top);
		}
	}
	

	public List<String> getLines() {
		return lines;
	}


}
