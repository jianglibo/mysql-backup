package com.go2wheel.mysqlbackup.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.go2wheel.mysqlbackup.exception.Md5ChecksumException;
import com.go2wheel.mysqlbackup.exception.RemoteFileNotAbsoluteException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.value.BackupedFiles;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHcommonUtil {

	private static Logger logger = LoggerFactory.getLogger(SSHcommonUtil.class);

	/**
	 * my.cnf -> my.cnf.1 -> my.cnf.2 -> my.cnf.3, origin file was keep.
	 * 
	 * @param session
	 * @param remoteFile
	 * @throws RunRemoteCommandException
	 * @throws IOException
	 * @throws JSchException
	 */
	public static void backupFile(Session session, String remoteFile) throws RunRemoteCommandException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getRemoteBackupedFiles(session, remoteFile);
		if (bfs.isOriginExists()) {
			runRemoteCommand(session, String.format("cp %s %s", remoteFile, remoteFile + "." + bfs.getNextInt()));
		}
	}

	/**
	 * my.cnf -> my.cnf.1 -> my.cnf.2 -> my.cnf.3, origin file was removed.
	 * 
	 * @param session
	 * @param remoteFile
	 * @throws RunRemoteCommandException
	 * @throws IOException
	 * @throws JSchException
	 */
	public static boolean backupFileByMove(Session session, String remoteFile) throws RunRemoteCommandException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getRemoteBackupedFiles(session, remoteFile);
		if (bfs.isOriginExists()) {
			RemoteCommandResult rcr = runRemoteCommand(session,
					String.format("mv %s %s", remoteFile, remoteFile + "." + bfs.getNextInt()));
			if (!rcr.isExitValueNotEqZero()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * delete all backup files but version 1. Version 1 is origin file.
	 * 
	 * @param session
	 * @param remoteFile
	 * @throws RunRemoteCommandException
	 */
	public static void deleteBackupedFiles(Session session, String remoteFile) throws RunRemoteCommandException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getRemoteBackupedFiles(session, remoteFile);
		List<String> backed = bfs.getBackups();
		if (backed.size() > 0) {
			backed.remove(0);
		}
		if (backed.size() > 0) {
			deleteRemoteFile(session, bfs.getBackups());
		}
	}

	public static void killProcess(Session session, String psGrep) throws RunRemoteCommandException {
		String command = String.format("ps -A | grep %s", psGrep);
		RemoteCommandResult rcr = runRemoteCommand(session, command);
		if (rcr.getExitValue() == 0) {
			if (rcr.getAllTrimedNotEmptyLines().size() == 1) {
				String line = rcr.getAllTrimedNotEmptyLines().get(0);
				String[] ss = line.split("\\s+");
				String seg = Stream.of(ss).map(s -> s.trim()).filter(s -> !s.isEmpty()).findFirst().get();
				if (seg.matches("\\d+")) {
					command = String.format("kill -n 9 %s", seg);
					SSHcommonUtil.runRemoteCommand(session, command);
				}
			}
		}
	}
	
	public static String targzFile(Session session, String remoteFile) throws RunRemoteCommandException {
		String tarFile = RemotePathUtil.getRidOfLastSlash(remoteFile) + ".tar.gz";
		String command = String.format("tar -czf %s %", tarFile, remoteFile);
		RemoteCommandResult rcr =  runRemoteCommand(session, command);
		if (rcr.getExitValue() == 0) {
			return tarFile;			
		} else {
			return null;
		}
	}

	/**
	 * By invoke 'ls remoteFile', if it's a directory, the first line should be
	 * 'total x'
	 * 
	 * @param session
	 * @param remoteFile
	 * @return
	 * @throws RunRemoteCommandException
	 */
	public static boolean isDirectory(Session session, String remoteFile) throws RunRemoteCommandException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		String command = String.format("ls -l %s", remoteFile);
		RemoteCommandResult rcr = runRemoteCommand(session, command);
		if (!rcr.isExitValueNotEqZero() && rcr.getAllTrimedNotEmptyLines().get(0).startsWith("total")) {
			return true;
		}
		return false;
	}

	// @formatter:off

	public static String getRemoteFileMd5(Session session, String remoteFile) throws RunRemoteCommandException {
		Optional<String[]> md5pair = runRemoteCommand(session, String.format("md5sum %s", remoteFile))
				.getAllTrimedNotEmptyLines().stream().map(l -> l.trim()).map(l -> l.split("\\s+"))
				.filter(pair -> pair.length == 2).filter(pair -> pair[1].equals(remoteFile) && pair[0].length() == 32)
				.findAny();
		return md5pair.get()[0];
	}

	public static int countFiles(Session session, String rfile) throws RunRemoteCommandException {
		RemoteCommandResult rcr = runRemoteCommand(session, String.format("find %s -type f | wc -l", rfile));
		return Integer.valueOf(rcr.getAllTrimedNotEmptyLines().get(0));
	}

	// public static

	public static int countAll(Session session, String rfile) throws RunRemoteCommandException {
		RemoteCommandResult rcr = runRemoteCommand(session, String.format("find %s | wc -l", rfile));
		return Integer.valueOf(rcr.getAllTrimedNotEmptyLines().get(0));
	}

	public static void downloadWithTmpDownloadingFile(Session session, String rfile, Path lfile)
			throws RunRemoteCommandException, IOException, ScpException {
		Path localDir = lfile.getParent();
		if (!Files.exists(localDir)) {
			Files.createDirectories(localDir);
		}
		Path localTmpFile = localDir.resolve(lfile.getFileName().toString() + ".downloading");
		ScpUtil.from(session, rfile, localTmpFile.toString());
		String remoteMd5 = getRemoteFileMd5(session, rfile);
		String localMd5 = Md5Checksum.getMD5Checksum(localTmpFile.toString());
		if (remoteMd5.equalsIgnoreCase(localMd5)) {
			Files.move(localTmpFile, lfile, StandardCopyOption.ATOMIC_MOVE);
		} else {
			throw new Md5ChecksumException();
		}
	}

	// @formatter:on

	/**
	 * returned backupfiles's backups list doesn't contain origin file.
	 * 
	 * @param session
	 * @param remoteFile
	 * @return
	 * @throws RunRemoteCommandException
	 */
	public static BackupedFiles getRemoteBackupedFiles(Session session, String remoteFile)
			throws RunRemoteCommandException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = new BackupedFiles(remoteFile);
		boolean isDirectory = isDirectory(session, remoteFile);
		List<String> fns;
		if (isDirectory) {
			String remoteFilep = RemotePathUtil.getParentWithEndingSlash(remoteFile);
			fns = runRemoteCommand(session, String.format("ls -p %s | grep /$", remoteFilep))
					.getAllTrimedNotEmptyLines().stream().map(line -> line.substring(0, line.length() - 1))
					.map(line -> {
						if (!line.startsWith("/")) {
							return remoteFilep + line;
						} else {
							return line;
						}
					}).collect(Collectors.toList());
		} else {
			fns = runRemoteCommand(session, String.format("ls -p %s | grep -v /$", remoteFile + "*"))
					.getAllTrimedNotEmptyLines();
		}

		if (fns == null || fns.isEmpty()) {
			bfs.setOriginExists(false);
		} else {
			bfs.setOriginExists(true);
			Pattern ptn = Pattern.compile(remoteFile + "\\.(\\d+)$");
			fns = fns.stream().filter(fn -> ptn.matcher(fn).matches()).collect(Collectors.toList()); // not include
																										// origin file.
			Collections.sort(fns, new Comparator<String>() {

				@Override
				public int compare(String s1, String s2) {
					if (s1.length() == s2.length()) {
						return s1.compareTo(s2);
					} else {
						return s1.length() - s2.length();
					}
				}
				
			});
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

	/**
	 * Make one step back. for example, xx.2 -> xx, then delete xx.2.
	 * 
	 * @param session
	 * @param remoteFile
	 * @throws RunRemoteCommandException
	 */
	public static void revertFile(Session session, String remoteFile) throws RunRemoteCommandException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getRemoteBackupedFiles(session, remoteFile);
		if (bfs.isOriginExists() && bfs.getNextInt() > 1) {
			runRemoteCommand(session, String.format("cp %s %s", remoteFile + "." + (bfs.getNextInt() - 1), remoteFile));
			deleteRemoteFile(session, remoteFile + "." + (bfs.getNextInt() - 1));
		}
	}

	/**
	 * to origin file. xx.1 is the origin file.
	 * 
	 * @param session
	 * @param remoteFile
	 * @throws IOException
	 * @throws JSchException
	 * @throws RunRemoteCommandException
	 */
	public static void revertFileToOrigin(Session session, String remoteFile)
			throws IOException, JSchException, RunRemoteCommandException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getRemoteBackupedFiles(session, remoteFile);
		if (bfs.isOriginExists() && bfs.getNextInt() > 1) {
			runRemoteCommand(session, String.format("cp %s %s", remoteFile + ".1", remoteFile));
		}
	}
	
	public static int coreNumber(Session session) {
		String command = "grep 'model name' /proc/cpuinfo | wc -l";
		try {
			RemoteCommandResult rcr = runRemoteCommand(session, command);
			if (rcr.getExitValue() == 0) {
				return Integer.valueOf(rcr.getStdOut().trim());
			} else {
				ExceptionUtil.logRemoteCommandResult(logger, rcr);
				return -1;
			}
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return -1;
		}
	}

	public static RemoteCommandResult runRemoteCommand(Session session, String command)
			throws RunRemoteCommandException {
		try {
			final Channel channel = session.openChannel("exec");
			try {
				((ChannelExec) channel).setCommand(command);

				channel.setInputStream(null);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				((ChannelExec) channel).setErrStream(baos);
				InputStream in = channel.getInputStream();
				channel.connect();
				RemoteCommandResult cmdOut = SSHcommonUtil.readChannelOutput(channel, in);
				String errOut = baos.toString();
				cmdOut.setErrOut(errOut);
				cmdOut.setCommand(command);
				return cmdOut;
			} finally {
				channel.disconnect();
			}
		} catch (JSchException | IOException e) {
			throw new RunRemoteCommandException(command, e.getMessage());
		}
	}

	public static void deleteRemoteFile(Session session, String remoteFile) throws RunRemoteCommandException {
		runRemoteCommand(session, String.format("rm %s", remoteFile));
	}

	public static void deleteRemoteFile(Session session, List<String> remoteFiles) throws RunRemoteCommandException {
		runRemoteCommand(session, String.format("rm %s", String.join(" ", remoteFiles)));
	}

	public static boolean fileExists(Session session, String rfile) throws RunRemoteCommandException {
		List<String> lines = runRemoteCommand(session, String.format("ls %s", rfile)).getAllTrimedNotEmptyLines();
		return !lines.stream().anyMatch(line -> line.indexOf("No such file or directory") != -1);
	}

	public static RemoteCommandResult readChannelOutput(final Channel channel, InputStream in) throws IOException {
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
		return RemoteCommandResult.partlyResult(sb.toString(), exitValue);
	}

	public static RemoteCommandResult readChannelOutputDoBest(final Channel channel, InputStream in, String ptn)
			throws IOException {
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
		return RemoteCommandResult.partlyResult(sb.toString(), exitValue);
	}
}
