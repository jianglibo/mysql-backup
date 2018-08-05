package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.model.BigOb;
import com.go2wheel.mysqlbackup.service.BigObDbService;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.FacadeResult;

@Service
public class SecurityService {
	
	@Autowired
	private MyAppSettings appSettings;

	@Autowired
	private BigObDbService bigObDbService;


	public FacadeResult<?> securityKeygen(String enc) throws ClassNotFoundException, SQLException {
	    Class.forName("org.hsqldb.jdbc.JDBCDriver");
	    Connection con = DriverManager.getConnection("jdbc:hsqldb:mem:abc", "SA", "");
	    Statement stmt = con.createStatement();  
	    ResultSet rs = stmt.executeQuery("CALL CRYPT_KEY('" + enc + "', null);");
	    rs.next();
	    String key1 = rs.getString(1);
		return FacadeResult.doneExpectedResultDone(key1);
	}
	
	public FacadeResult<?> securityCopySshkey(boolean toFile, boolean deleteFile) throws ClassNotFoundException, IOException {
		String idrsa = appSettings.getSsh().getSshIdrsa();
		Path idrsaPath = Paths.get(idrsa);
		BigOb bo = bigObDbService.findByName(BigOb.SSH_KEYFILE_NAME);
		if (toFile) {
			if (bo == null) {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, BigOb.SSH_KEYFILE_NAME);
			} else {
				Files.write(idrsaPath, bo.getContent());
			}
		} else {
			byte[] bytes = Files.readAllBytes(idrsaPath);
			if (bo == null) {
				bo = new BigOb();
				bo.setContent(bytes);
				bo.setCreatedAt(new Date());
				bo.setName(BigOb.SSH_KEYFILE_NAME);
				bigObDbService.save(bo);
			} else {
				bo.setContent(bytes);
				bigObDbService.save(bo);
			}
		}
		return FacadeResult.doneExpectedResult();
	}

	public FacadeResult<?> securityCopyKnownHosts(boolean toFile) throws IOException {
		String knownHosts = appSettings.getSsh().getKnownHosts();
		Path knownHostsPath = Paths.get(knownHosts);
		BigOb bo = bigObDbService.findByName(BigOb.SSH_KNOWN_HOSTS);
		if (toFile) {
			if (bo == null) {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, BigOb.SSH_KNOWN_HOSTS);
			} else {
				Files.write(knownHostsPath, bo.getContent());
			}
		} else {
			byte[] bytes = Files.readAllBytes(knownHostsPath);
			if (bo == null) {
				bo = new BigOb();
				bo.setContent(bytes);
				bo.setCreatedAt(new Date());
				bo.setName(BigOb.SSH_KNOWN_HOSTS);
				bigObDbService.save(bo);
			} else {
				bo.setContent(bytes);
				bigObDbService.save(bo);
			}
		}
		return FacadeResult.doneExpectedResult();
	}
}
