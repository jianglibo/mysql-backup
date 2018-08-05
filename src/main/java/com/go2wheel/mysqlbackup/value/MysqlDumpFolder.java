package com.go2wheel.mysqlbackup.value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;

public class MysqlDumpFolder implements Comparable<MysqlDumpFolder> {
	
	private static Logger logger = LoggerFactory.getLogger(MysqlDumpFolder.class);

	private static final Pattern indexPtn = Pattern.compile("(.*)\\.\\d+$");
	
	private String id;

	private final Path folder;
	
	private long dumpSize;
	
	private int logFiles;
	
	private long logFileSize;

	public MysqlDumpFolder(Path folder) throws IOException, UnExpectedInputException {
		this.folder = folder;
		guessBaseName();
	}
	
	public static MysqlDumpFolder newInstance(Path folder) {
		try {
			return new MysqlDumpFolder(folder);
		} catch (IOException | UnExpectedInputException e) {
			ExceptionUtil.logErrorException(logger, e);
			return null;
		}
	}

	private void guessBaseName() throws IOException, UnExpectedInputException {
		List<Path> pathes = Files.list(folder).filter(p -> indexPtn.matcher(p.toAbsolutePath().toString()).matches()).collect(Collectors.toList());
		Collections.sort(pathes, PathUtil.PATH_NAME_DESC);
		
		this.id = folder.getFileName().toString();
		
		Path dumpsql = Files.list(folder).filter(p -> p.toString().endsWith(".sql")).findAny().orElse(null);
		if (dumpsql == null) {
			throw new UnExpectedInputException("10000", "mysql.dumpfolder.empty", pathes.size() + "");
		}
		
		this.dumpSize = Files.size(dumpsql);
		
		if (pathes.size() > 0) {
			List<String> lines = Files.readAllLines(pathes.get(0));
			this.logFiles = lines.size();
			this.logFileSize = lines.stream().mapToLong(line -> {
				try {
					return Files.size(folder.resolve(line));
				} catch (IOException e) {
					return 0L;
				}
			}).sum();
		}
	}
	

	public long getDumpSize() {
		return dumpSize;
	}

	public void setDumpSize(long dumpSize) {
		this.dumpSize = dumpSize;
	}

	public int getLogFiles() {
		return logFiles;
	}

	public void setLogFiles(int logFiles) {
		this.logFiles = logFiles;
	}


	public long getLogFileSize() {
		return logFileSize;
	}

	public void setLogFileSize(long logFileSize) {
		this.logFileSize = logFileSize;
	}

	public Path getFolder() {
		return folder;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int compareTo(MysqlDumpFolder o) {
		return o.getFolder().getFileName().toString().compareTo(getFolder().getFileName().toString());
	}

}
