package com.go2wheel.mysqlbackup.executablerunner;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public interface ExecutableRunner<T> {
	
	ExecuteResult<T> execute(String...commandWords);
	
	
	default List<String> getWhatProcessWriteOut(InputStream outFromProcess) throws IOException {
	    BufferedReader reader = 
                new BufferedReader(new InputStreamReader(outFromProcess));
	    List<String> lines = new ArrayList<>();
		String line = null;
		while ( (line = reader.readLine()) != null) {
		   lines.add(line);
		}
		return lines;
	}
	
	default PrintWriter getProcessWriter(OutputStream os) {
		PrintWriter pw = new PrintWriter(new BufferedOutputStream(os));
		return pw;
	}
 
}
