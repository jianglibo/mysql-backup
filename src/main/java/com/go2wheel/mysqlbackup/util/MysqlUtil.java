package com.go2wheel.mysqlbackup.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.executablerunner.ExecutableRunnerSsh;
import com.go2wheel.mysqlbackup.mysqlcfg.MyCnfContentGetter;
import com.go2wheel.mysqlbackup.mysqlcfg.MyCnfFirstExist;
import com.go2wheel.mysqlbackup.mysqlcfg.MysqlCnfFileLister;
import com.go2wheel.mysqlbackup.value.ExternalExecuteResult;
import com.go2wheel.mysqlbackup.value.MyCnfHolder;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

import net.schmizz.sshj.SSHClient;

@Component
public class MysqlUtil {
	
	private SshClientFactory sshClientFactory;
	
	public static <T> ExternalExecuteResult<T> runListOfCommands(ExecutableRunnerSsh<T>...commands) {
		ExternalExecuteResult<T> er = null;
		for(ExecutableRunnerSsh<T> command: commands) {
			command.setPrevResult(er);
			er = command.execute();
			if (!er.isSuccess()) {
				return er;
			}
		}
		return er;
	}
	
	public MyCnfHolder getMycnf(MysqlInstance instance) {
		SSHClient sshClient;
		sshClient = sshClientFactory.getConnectedSSHClient(instance).get();
		ExternalExecuteResult<List<String>> er = runListOfCommands(
				new MysqlCnfFileLister(sshClient, instance),
				new MyCnfFirstExist(sshClient, instance),
				new MyCnfContentGetter(sshClient, instance)
				);
		List<String> lines = er.getResult();
		return new MyCnfHolder(lines);
	}

	
	@Autowired
	public void setSshClientFactory(SshClientFactory sshClientFactory) {
		this.sshClientFactory = sshClientFactory;
	}
	
	

}
