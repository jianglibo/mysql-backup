package com.go2wheel.mysqlbackup.installer;

import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.service.SoftwareDbService;
import com.go2wheel.mysqlbackup.util.StringUtil;

public abstract class InstallerBase<I extends InstallInfo> implements Installer<I> {
	
	@Autowired
	protected SoftwareDbService softwareDbService;
	
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
}
