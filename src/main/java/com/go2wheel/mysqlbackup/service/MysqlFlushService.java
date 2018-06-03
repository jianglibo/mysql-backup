package com.go2wheel.mysqlbackup.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.jooq.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlFlushRecord;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.repository.MysqlFlushRepository;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.ResultEnum;

@Service
@Validated
public class MysqlFlushService extends ServiceBase<MysqlFlushRecord, MysqlFlush> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ServerService serverService;

	public MysqlFlushService(MysqlFlushRepository repo) {
		super(repo);
	}
	
	public void processFlushResult(Server box, FacadeResult<String> fr) {
		Server sv = serverService.findByHost(box.getHost());
		MysqlFlush mf = new MysqlFlush();
		mf.setServerId(sv.getId());
		mf.setCreatedAt(new Date());
		mf.setTimeCost(fr.getEndTime() - fr.getStartTime());
		if (fr.isExpected() && fr.getResult() != null) {
			try {
				Path p = Paths.get(fr.getResult());
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
				mf.setResult(ResultEnum.FAIL);
			}
		} else {
			mf.setResult(ResultEnum.FAIL);
		}
		save(mf);
	}

}
