package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.exception.RemoteFileNotAbsoluteException;
import com.go2wheel.mysqlbackup.value.BackupedFiles;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHcommonUtil {
	
	/**
	 * my.cnf -> my.cnf.1 -> my.cnf.2 -> my.cnf.3
	 * @param session
	 * @param remoteFile
	 * @throws IOException 
	 * @throws JSchException 
	 */
	public static void backupFile(Session session, String remoteFile) throws IOException, JSchException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getBackupedFiles(session, remoteFile);
		if (bfs.isOriginExists()) {
			runRemoteCommand(session, String.format("cp %s %s",remoteFile, remoteFile + "." + bfs.getNextInt()));
		}
	}
	
	public static void deleteBackupedFiles(Session session, String remoteFile) throws IOException, JSchException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getBackupedFiles(session, remoteFile);
		List<String> backed = bfs.getBackups();
		if (backed.size() > 0) {
			backed.remove(0);
		}
		if (backed.size() > 0) {
			deleteRemoteFile(session, bfs.getBackups());
		}
	}
	
	public static BackupedFiles getBackupedFiles(Session session, String remoteFile) throws IOException, JSchException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = new BackupedFiles(remoteFile);
		List<String> fns = runRemoteCommandAndGetList(session, String.format("ls -p %s | grep -v /$", remoteFile + "*"));
		if (fns == null || fns.isEmpty()) {
			bfs.setOriginExists(false);
		} else {
			bfs.setOriginExists(true);
			Pattern ptn = Pattern.compile(remoteFile + "\\.(\\d+)$");
			fns = fns.stream().filter(fn -> ptn.matcher(fn).matches()).collect(Collectors.toList()); // not include origin file.
			Collections.sort(fns);
			if (fns.size() > 0) {
				Matcher m = ptn.matcher(fns.get(fns.size() - 1));
				m.matches();
				bfs.setNextInt(Integer.valueOf(m.group(1)) + 1);
			} else {
				bfs.setNextInt(1);
			}
			bfs.setBackups(fns);
		}
		return bfs;
	}
	
	public static void deketeBackupFiles(Session session, String remoteFile) throws IOException, JSchException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getBackupedFiles(session, remoteFile);
		if (bfs.isOriginExists() && bfs.getNextInt() > 1) {
			runRemoteCommand(session, String.format("cp %s %s", remoteFile + "." + (bfs.getNextInt() - 1), remoteFile));
			deleteRemoteFile(session, remoteFile + "." + (bfs.getNextInt() - 1));
		}
	}
	
	public static void revertFile(Session session, String remoteFile) throws IOException, JSchException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getBackupedFiles(session, remoteFile);
		if (bfs.isOriginExists() && bfs.getNextInt() > 1) {
			runRemoteCommand(session, String.format("cp %s %s", remoteFile + "." + (bfs.getNextInt() - 1), remoteFile));
			deleteRemoteFile(session, remoteFile + "." + (bfs.getNextInt() - 1));
		}
	}
	
	public static void revertFileToOrigin(Session session, String remoteFile) throws IOException, JSchException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getBackupedFiles(session, remoteFile);
		if (bfs.isOriginExists() && bfs.getNextInt() > 1) {
			runRemoteCommand(session, String.format("cp %s %s", remoteFile + ".1", remoteFile));
		}
	}
	
	public static String runRemoteCommand(Session sshSession, String command) throws IOException, JSchException {
		final Channel channel = sshSession.openChannel("exec");
		try {
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();
			RemoteCommandResult<String> cmdOut = SSHcommonUtil.readChannelOutput(channel, in);
			return cmdOut.getResult();
		} finally {
			channel.disconnect();
		}
	}
	
	public static List<String> runRemoteCommandAndGetList(Session sshSession, String command) throws IOException, JSchException {
		return StringUtil.splitLines(runRemoteCommand(sshSession, command)).stream().filter(line -> !line.trim().isEmpty()).collect(Collectors.toList());
	}

//	public static String getRemoteFileContent(session session, String remoteFile) throws IOException {
//        final SFTPClient sftp = session.newSFTPClient();
//        Set<OpenMode> om = new HashSet<>();
//        om.add(OpenMode.READ);
//        RemoteFile rf = sftp.open(remoteFile, om);
//        long fl = rf.length();
//        byte[] bytes = new byte[(int) fl];
//        int readResult = rf.read(0, bytes, 0, (int) fl);
//        rf.close();
//        return new String(bytes);
//	}
	
	
	public static void deleteRemoteFile(Session sshSession, String remoteFile) throws IOException, JSchException {
		runRemoteCommand(sshSession, String.format("rm %s", remoteFile));
	}
	
	public static void deleteRemoteFile(Session sshSession, List<String> remoteFiles) throws IOException, JSchException {
		runRemoteCommand(sshSession, String.format("rm %s", String.join(" ",remoteFiles)));
	}
	
//	public static void writeRemoteFile(session session, String remoteFile, String content) throws IOException {
//        session.useCompression();
//        Path tmp = Files.createTempFile("writeremtefile", null);
//        Files.write(tmp, content.getBytes());
//        session.newSCPFileTransfer().upload(new FileSystemFile(tmp.toFile()), remoteFile);
//        try {
//			Files.delete(tmp);
//		} catch (Exception e) {
//		}
//	}

	
//	public static void touchAfile(Session sshSession, String remoteFile) throws IOException, JSchException {
//		runRemoteCommand(sshSession, String.format("touch %s", remoteFile));
//	}
	
	public static RemoteCommandResult<String> readChannelOutput(final Channel channel, InputStream in) throws IOException {
		StringBuffer sb = new StringBuffer();
		int exitValue = 0;
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				sb.append(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				if (in.available() > 0)
					continue;
				exitValue = channel.getExitStatus();
				break;
			}
		}
		return new RemoteCommandResult<String>(sb.toString(), exitValue);
	}
	
	public static RemoteCommandResult<String> readChannelOutputDoBest(final Channel channel, InputStream in, String ptn) throws IOException {
		StringBuffer sb = new StringBuffer();
		int exitValue = 0;
		byte[] tmp = new byte[1024];
		long startTime = System.currentTimeMillis();
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				sb.append(new String(tmp, 0, i));
				startTime = System.currentTimeMillis();
			}
			if (channel.isClosed()) {
				if (in.available() > 0)
					continue;
				exitValue = channel.getExitStatus();
				break;
			}
			Pattern pp = Pattern.compile(ptn, Pattern.DOTALL);
			Matcher m = pp.matcher(sb.toString().trim()); 
			if (m.matches()) {
				break;
			}
			if ((System.currentTimeMillis() - startTime) > 3000) {
				break;
			}
		}
		return new RemoteCommandResult<String>(sb.toString(), exitValue);
	}
}
