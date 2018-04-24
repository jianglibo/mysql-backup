package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.mysqlcfg.MyCnfContentGetter;
import com.go2wheel.mysqlbackup.mysqlcfg.MyCnfFirstExist;
import com.go2wheel.mysqlbackup.mysqlcfg.MysqlCnfFileLister;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.value.MyCnfHolder;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

import net.schmizz.sshj.SSHClient;

@Component
public class MysqlUtil {
	
	private SshClientFactory sshClientFactory;
	
	private MyAppSettings appSettings;

	public MyCnfHolder getMycnf(MysqlInstance instance) {
		SSHClient sshClient;
		sshClient = sshClientFactory.getConnectedSSHClient(instance).get();
		
		RemoteCommandResult<List<String>> er = ExecutorUtil.runListOfCommands(Arrays.asList(
				new MysqlCnfFileLister(sshClient, instance),
				new MyCnfFirstExist(sshClient, instance),
				new MyCnfContentGetter(sshClient, instance)));
		List<String> lines = er.getResult();
		return new MyCnfHolder(lines);
	}
	
	public void writeDescription(MysqlInstance instance) throws IOException {
		String ds = YamlInstance.INSTANCE.getYaml().dumpAsMap(instance);
		Path dstDir = appSettings.getDataRoot().resolve(instance.getHost());
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		Path dstFile = dstDir.resolve(BackupCommand.DESCRIPTION_FILENAME);
		Files.write(dstFile, ds.getBytes());
	}
	
	public Path getDescriptionFile(MysqlInstance instance) {
		return appSettings.getDataRoot().resolve(instance.getHost()).resolve(BackupCommand.DESCRIPTION_FILENAME);
	}

	
	@Autowired
	public void setSshClientFactory(SshClientFactory sshClientFactory) {
		this.sshClientFactory = sshClientFactory;
	}
	
	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
	
	

}
