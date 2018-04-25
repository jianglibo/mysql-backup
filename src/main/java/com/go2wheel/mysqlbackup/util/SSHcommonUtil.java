package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHcommonUtil {
	
	/**
	 * my.cnf -> my.cnf.1 -> my.cnf.2 -> my.cnf.3
	 * @param sshClient
	 * @param remoteFile
	 * @throws IOException 
	 * @throws JSchException 
	 */
	public static void backupFile(Session sshClient, String remoteFile) throws IOException, JSchException {
		List<String> fns = runRemoteCommandAndGetList(sshClient, String.format("ls -p %s | grep -v /$", remoteFile + "*"));
		if (fns == null || fns.isEmpty()) {
			return;
		}
		fns = fns.stream().filter(fn -> fn.startsWith(remoteFile)).collect(Collectors.toList());
		Collections.sort(fns);
		Pattern ptn = Pattern.compile(remoteFile + "\\.(\\d+)$");
		Matcher m = ptn.matcher(fns.get(fns.size() - 1));
		int i = 1;
		if (m.matches()) {
			i = Integer.valueOf(m.group(1)) + 1;
		}
		runRemoteCommand(sshClient, String.format("cp %s %s",remoteFile, remoteFile + "." + i));
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

//	public static String getRemoteFileContent(SSHClient sshClient, String remoteFile) throws IOException {
//        final SFTPClient sftp = sshClient.newSFTPClient();
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
	
//	public static void writeRemoteFile(SSHClient sshClient, String remoteFile, String content) throws IOException {
//        sshClient.useCompression();
//        Path tmp = Files.createTempFile("writeremtefile", null);
//        Files.write(tmp, content.getBytes());
//        sshClient.newSCPFileTransfer().upload(new FileSystemFile(tmp.toFile()), remoteFile);
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
