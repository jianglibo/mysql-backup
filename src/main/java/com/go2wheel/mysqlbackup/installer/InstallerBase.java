package com.go2wheel.mysqlbackup.installer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.service.SoftwareDbService;
import com.go2wheel.mysqlbackup.service.SoftwareInstallationDbService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.Session;

public abstract class InstallerBase<I extends InstallInfo> implements Installer<I> {
	
	@Autowired
	protected SoftwareDbService softwareDbService;
	
	@Autowired
	protected SettingsInDb settingsInDb;
	
	@Autowired
	protected SshSessionFactory sshSessionFactory;
	
	
	@Autowired
	protected SoftwareInstallationDbService softwareInstallationDbService;
	
	@Autowired
	protected FileDownloader fileDownloader;
	
	protected void saveToDb(Software software) {
		Software indb = softwareDbService.findByUniqueField(software);
		
		if (indb == null) {
			softwareDbService.save(software);
		} else {
			if (!StringUtil.stringEqual(indb.getWebsite(), software.getWebsite()) ||
					!StringUtil.stringEqual(indb.getDlurl(), software.getDlurl()) ||
					!StringUtil.stringEqual(indb.getInstaller(), software.getInstaller())) {
				indb.setDlurl(software.getDlurl());
				indb.setInstaller(software.getInstaller());
				indb.setWebsite(software.getWebsite());
				softwareDbService.save(indb);
			}
		}
	}
	
	public void getLocalBinaryOrDownload(Session session, Software software) throws ScpException, IOException {
		Path localPath = settingsInDb.getDownloadPath().resolve(software.getInstaller());
		if (!Files.exists(localPath)) { 
			fileDownloader.download(software.getDlurl(), localPath);
		}
}
	
	
	public Path getLocalInstallerPath(Software software) {
		return settingsInDb.getDownloadPath().resolve(software.getInstaller());
	}
	
	public Session getSession(Server server) {
		FacadeResult<Session> fr = sshSessionFactory.getConnectedSession(server);
		if (fr.isExpected()) {
			return fr.getResult();
		} else {
			throw new RuntimeException("ssh connect failed.");
		}
	}

}
