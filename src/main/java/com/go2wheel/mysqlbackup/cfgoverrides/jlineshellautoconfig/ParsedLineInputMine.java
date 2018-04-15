package com.go2wheel.mysqlbackup.cfgoverrides.jlineshellautoconfig;

import java.util.List;

import org.jline.reader.ParsedLine;
import org.springframework.shell.Input;

public class ParsedLineInputMine implements Input {

    private final ParsedLine parsedLine;

    ParsedLineInputMine(ParsedLine parsedLine) {
        this.parsedLine = parsedLine;
    }

    @Override
    public String rawText() {
        return parsedLine.line();
    }

    @Override
    public List<String> words() {
        return JLineShellAutoConfigurationMine.sanitizeInput(parsedLine.words());
    }
}
