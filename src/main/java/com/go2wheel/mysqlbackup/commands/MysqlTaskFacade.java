package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.exception.EnableLogBinFailedException;
import com.go2wheel.mysqlbackup.expect.MysqlDumpExpect;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.StringUtil.LinuxFileInfo;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class MysqlTaskFacade {
	
	private MysqlUtil mysqlUtil;

	@Autowired
	public void setMysqlUtil(MysqlUtil mysqlUtil) {
		this.mysqlUtil = mysqlUtil;
	}
	
	public String mysqlDump(Session session, Box box) throws JSchException, IOException {
		Optional<LinuxFileInfo> ll = new MysqlDumpExpect(session, box).start();
		if (ll.isPresent()) {
			mysqlUtil.downloadDumped(session, box, ll.get());
			return String.format("mysqldump到%s, 长度：%s", ll.get().getFilename(), ll.get().getSize());
		} else {
			return "mysqldump失败";
		}
	}

	public String mysqlEnableLogbin(Session session, Box box, String logBinValue) throws JSchException, IOException {
		LogBinSetting lbs = box.getMysqlInstance().getLogBinSetting();
		if (lbs != null && lbs.isEnabled()) {
			return "本地服务器描述显示LogBin已经启用。";
		} else {
			lbs = mysqlUtil.getLogbinState(session, box);
			if (lbs.isEnabled()) {
				box.getMysqlInstance().setLogBinSetting(lbs);
				mysqlUtil.writeDescription(box);
				return "本地服务器描述显示LogBin未启用，但远程显示已经启动，修改本地描述。";
			} else {
				MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, box); // 找到起作用的my.cnf配置文件。
				String mycnfFile = box.getMysqlInstance().getMycnfFile();
				mfh.enableBinLog(logBinValue); // 修改logbin的值
				SSHcommonUtil.backupFile(session, mycnfFile); // 先备份配置文件， my.cnf -> my.cnf.1
				ScpUtil.to(session, mycnfFile, mfh.toByteArray()); // 覆盖写入 my.cnf
				mysqlUtil.restartMysql(session); // 重启Mysql
				lbs = mysqlUtil.getLogbinState(session, box); // 获取最新的logbin状态。
				if (!lbs.isEnabled()) {
					throw new EnableLogBinFailedException(box.getHost());
				}
				box.getMysqlInstance().setLogBinSetting(lbs);
				mysqlUtil.writeDescription(box); //保存
			}
		}
		return lbs.toString();
	}
}
