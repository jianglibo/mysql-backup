package com.go2wheel.mysqlbackup.dbservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlFlushRecord;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.repository.MysqlFlushRepository;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;

@Service
@Validated
public class MysqlFlushDbService extends DbServiceBase<MysqlFlushRecord, MysqlFlush> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public MysqlFlushDbService(MysqlFlushRepository repo) {
		super(repo);
	}
	/**
	 * 接收到的是index文件的路径，将目前所有的bin日志统计出来。
	 * @param server
	 * @param fr
	 */
	public void processFlushResult(Server server, FacadeResult<Path> fr) {
		MysqlFlush mf = new MysqlFlush();
		mf.setServerId(server.getId());
		mf.setCreatedAt(new Date());
		mf.setTimeCost(fr.getEndTime() - fr.getStartTime());
		if (fr.isExpected() && fr.getResult() != null) {
			try {
				Path p = fr.getResult();
				List<String> files = Files.readAllLines(p);
				mf.setFileNumber(files.size());
				final Path pp = p.getParent();
				long ts = files.stream().map(pp::resolve).mapToLong(arg0 -> {
					try {
						return Files.size(arg0);
					} catch (IOException e) {
						ExceptionUtil.logErrorException(logger, e);
						return 0;
					}
				}).sum();
				mf.setFileSize(ts);
			} catch (IOException e) {
				ExceptionUtil.logErrorException(logger, e);
			}
		} else {
		}
		save(mf);
	}

}
