package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;

public class SSHcommonUtil {

	public static String getRemoteFileContent(SSHClient sshClient, String remoteFile) throws IOException {
        final SFTPClient sftp = sshClient.newSFTPClient();
        Set<OpenMode> om = new HashSet<>();
        om.add(OpenMode.READ);
        RemoteFile rf = sftp.open(remoteFile, om);
        long fl = rf.length();
        byte[] bytes = new byte[(int) fl];
        int readResult = rf.read(0, bytes, 0, (int) fl);
        rf.close();
        return new String(bytes);
	}
	
	
	public static void deleteRemoteFile(SSHClient sshClient, String remoteFile) throws IOException {
		final Session session = sshClient.startSession();
		try {
			final Command cmd = session.exec(String.format("rm %s", remoteFile));
		} finally {
			session.close();
		}
	}
}
