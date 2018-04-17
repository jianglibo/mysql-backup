package com.go2wheel.mysqlbackup.value;

import java.nio.file.Path;
import java.util.List;

public class ListInstanceResult extends ExecuteResult<List<Path>> {

	public ListInstanceResult(List<Path> result) {
		super(result);
	}

}
