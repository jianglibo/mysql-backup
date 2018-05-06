package com.go2wheel.mysqlbackup.value;

import java.util.ArrayList;
import java.util.List;

public class BorgPruneResult {
	
//			Keeping archive: ARCHIVE-2018-05-06-12-20-39          Sun, 2018-05-06 04:20:40 [981106628c86b6a582b00ae1cc6434d40ad51e0cebd0f4bd77a638138154ea66]
//			Pruning archive: ARCHIVE-2018-05-06-12-20-37          Sun, 2018-05-06 04:20:38 [a4c68531f595c622c3bfb249e0e004286f9573d8a89934c8898ba6f9702493bc] (1/9)
//			Pruning archive: ARCHIVE-2018-05-06-12-20-35          Sun, 2018-05-06 04:20:36 [fead51dd2b811affdd857e3523147e5a871781d206c542ff9aa67b521b9138a8] (2/9)
//			Pruning archive: ARCHIVE-2018-05-06-12-20-33          Sun, 2018-05-06 04:20:33 [e8d4b504273ff52181b7467fe1860d9e11d2d6e74ac2e780586372223a7785e8] (3/9)
//			Pruning archive: ARCHIVE-2018-05-06-12-20-31          Sun, 2018-05-06 04:20:31 [2a9a3fb791f70413123cbff8a7fdd20f23e1d1250494fdf67566cb1bc530d0e0] (4/9)
//			Pruning archive: ARCHIVE-2018-05-06-12-20-29          Sun, 2018-05-06 04:20:29 [36692a3c7eee37cef2f92b5632bb49b7cc080bd6e84c8f0d24ae898c98dea07c] (5/9)
//			Pruning archive: ARCHIVE-2018-05-06-12-20-26          Sun, 2018-05-06 04:20:27 [f55e2f1447ea124b2c3f492d6383393c6fa47a7cdb7c5617be3750842d84be6e] (6/9)
//			Pruning archive: ARCHIVE-2018-05-06-12-20-24          Sun, 2018-05-06 04:20:25 [c9a0834c171e16a5b5340836af8d0b3a9f68ce396ed2b0bb82219f8758e21d59] (7/9)
//			Pruning archive: ARCHIVE-2018-05-06-12-20-22          Sun, 2018-05-06 04:20:23 [8b98917d423991c6d242efca2906f35223476d27e5f392687c7e19ddc5c20561] (8/9)
//			Pruning archive: ARCHIVE-2018-05-06-12-20-12          Sun, 2018-05-06 04:20:13 [f0e57172548e1986d329077c4a515f9e772474b3ad5c3b1a432c56579342176e] (9/9)
//			terminating with success status, rc 0
	
	private List<String> lines = new ArrayList<>();
	
	public BorgPruneResult(RemoteCommandResult rcr) {
		if (rcr.getExitValue() == 0) {
			this.lines = rcr.getAllTrimedNotEmptyLines();			
		}
	}
	
	public boolean isSuccess() {
		return lines.stream().anyMatch(line -> line.contains("terminating with success status"));
	}
	
	public long prunedArchiveNumbers() {
		return lines.stream().filter(line -> line.contains("Pruning archive")).count();
	}
	
	public long keepedArchiveNumbers() {
		return lines.stream().filter(line -> line.contains("Keeping archive")).count();
	}
	

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
	}
	
	


	
}
