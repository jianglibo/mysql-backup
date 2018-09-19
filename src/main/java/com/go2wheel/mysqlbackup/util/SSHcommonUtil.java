package com.go2wheel.mysqlbackup.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.value.BackupedFiles;
import com.go2wheel.mysqlbackup.value.FileToCopyInfo;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.OsTypeWrapper;
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
	public static void backupFile(Session session, Server server, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		if(OsTypeWrapper.of(server.getOs()).isWin()) {
			backupFileWin(session, remoteFile);
		} else {
			BackupedFiles bfs = getRemoteBackupedFiles(session, remoteFile);
			if (bfs.isOriginExists()) {
				runRemoteCommand(session, String.format("cp %s %s", remoteFile, remoteFile + "." + bfs.getNextInt()));
			}
		}
	}
	
	
	/**
	 * my.cnf -> my.cnf.1 -> my.cnf.2 -> my.cnf.3, origin file was keep.
	 * 
	 * @param session
	 * @param remoteFile
	 * @throws RunRemoteCommandException
	 * @throws IOException
	 * @throws JSchException
	 */
	private static void backupFileWin(Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		String bfs = getRemoteBackupedFilesWin(session, remoteFile);
		logger.info(bfs);
	}

	public static boolean echo(SshSessionFactory sshSessionFactory, Server server)
			throws RunRemoteCommandException, JSchException, IOException {
		Session session = null;
		try {
			session = sshSessionFactory.getConnectedSession(server).getResult();
			RemoteCommandResult rcr = runRemoteCommand(session, "echo hello");
			String echoed = rcr.getAllTrimedNotEmptyLines().get(0);
			return "hello".equals(echoed);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}

	}

	public static RemoteCommandResult mkdirsp(String os, Session session, String remoteDir) throws RunRemoteCommandException, JSchException, IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteDir);
		OsTypeWrapper otw = OsTypeWrapper.of(os);
		if (otw.isWin()) {
			return runRemoteCommand(session, String.format("New-Item -Path %s -ItemType Directory", remoteDir));
		} else {
			return runRemoteCommand(session, String.format("mkdir -p %s", remoteDir));
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
	public static boolean backupFileByMove(Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		remoteFile = PathUtil.getRidOfLastSlash(remoteFile);
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
	 * @throws IOException 
	 * @throws JSchException 
	 */
	public static void deleteBackupedFiles(Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
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

	public static void killProcess(Session session, String psGrep) throws RunRemoteCommandException, JSchException, IOException {
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

	public static String targzFile(Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
		String tarFile = PathUtil.getRidOfLastSlash(remoteFile) + ".tar.gz";
		String command = String.format("tar -czf %s %s", tarFile, remoteFile);
		RemoteCommandResult rcr = runRemoteCommand(session, command);
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
	 * @throws IOException 
	 * @throws JSchException 
	 */
	public static boolean isDirectory(Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		String command = String.format("ls -l %s", remoteFile);
		RemoteCommandResult rcr = runRemoteCommand(session, command);
		if (!rcr.isExitValueNotEqZero() && rcr.getAllTrimedNotEmptyLines().get(0).startsWith("total")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Test-Path -Type Container remoteFile
	 * 
	 * @param session
	 * @param remoteFile
	 * @return
	 * @throws RunRemoteCommandException
	 * @throws IOException 
	 * @throws JSchException 
	 */
	public static boolean isDirectoryWin(Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		String command = String.format("Test-Path -Type Container %s", remoteFile);
		RemoteCommandResult rcr = runRemoteCommand(session, command);
		List<String> lines = rcr.getAllTrimedNotEmptyLines();
		return lines.size() == 1 && "TRUE".equalsIgnoreCase(lines.get(0)); 
	}

	// @formatter:off
	
	public static String getRemoteFileMd5(String os, Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
		if (OsTypeWrapper.of(os).isWin()) {
			String cmd = String.format("Get-FileHash -Path %s |Format-List", remoteFile);
			RemoteCommandResult rcr = runRemoteCommand(session, cmd);
			return PSUtil.parseFormatList(rcr.getAllTrimedNotEmptyLines()).get(0).get("Hash");
		} else {
			Optional<String[]> md5pair = runRemoteCommand(session, String.format("md5sum %s", remoteFile))
					.getAllTrimedNotEmptyLines().stream().map(l -> l.trim()).map(l -> l.split("\\s+"))
					.filter(pair -> pair.length == 2).filter(pair -> pair[1].equals(remoteFile) && pair[0].length() == 32)
					.findAny();
			return md5pair.get()[0];
		}
	}

//	public static String getRemoteFileMd5(Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
//		Optional<String[]> md5pair = runRemoteCommand(session, String.format("md5sum %s", remoteFile))
//				.getAllTrimedNotEmptyLines().stream().map(l -> l.trim()).map(l -> l.split("\\s+"))
//				.filter(pair -> pair.length == 2).filter(pair -> pair[1].equals(remoteFile) && pair[0].length() == 32)
//				.findAny();
//		return md5pair.get()[0];
//	}

	public static int countFiles(Session session, String rfile) throws RunRemoteCommandException, JSchException, IOException {
		RemoteCommandResult rcr = runRemoteCommand(session, String.format("find %s -type f | wc -l", rfile));
		return Integer.valueOf(rcr.getAllTrimedNotEmptyLines().get(0));
	}

	// public static

	public static int countAll(Session session, String rfile) throws RunRemoteCommandException, JSchException, IOException {
		RemoteCommandResult rcr = runRemoteCommand(session, String.format("find %s | wc -l", rfile));
		return Integer.valueOf(rcr.getAllTrimedNotEmptyLines().get(0));
	}
	
//    private static boolean isFileClosed(File file) {  
//        boolean closed;
//        java.nio.channels.Channel channel = null;
//        try {
//            channel = new RandomAccessFile(file, "rw").getChannel();
//            closed = true;
//        } catch(Exception ex) {
//            closed = false;
//        } finally {
//            if(channel!=null) {
//                try {
//                    channel.close();
//                } catch (IOException ex) {
//                    // exception handling
//                }
//            }
//        }
//        return closed;
//    }
	
	public static String getContent(Session session, String rfile) throws JSchException, IOException, ScpException {
		return ScpUtil.from(session, rfile).toString();
	}
	
	/**
	 * 
	 * @param os
	 * @param session
	 * @param rfile
	 * @param lfile
	 * @param postfix
	 * @return
	 * @throws RunRemoteCommandException
	 * @throws IOException
	 * @throws ScpException
	 * @throws JSchException
	 * @throws NoSuchAlgorithmException
	 */
	public static Path downloadWithTmpDownloadingFile(String os, Session session, String rfile, Path lfile, int postfix)
			throws RunRemoteCommandException, IOException, ScpException, JSchException, NoSuchAlgorithmException {
		return downloadWithTmpDownloadingFileWithNewVersion(os, session, rfile, null, lfile, postfix);
	}

	
	/**
	 * Download file with known md5.
	 * @param session
	 * @param rfile
	 * @param remoteMd5
	 * @param lfile
	 * @param postfix
	 * @return
	 * @throws RunRemoteCommandException
	 * @throws IOException
	 * @throws ScpException
	 * @throws JSchException
	 * @throws NoSuchAlgorithmException
	 */
	public static Path downloadWithTmpDownloadingFileWithNewVersion(String os, Session session, String rfile, String remoteMd5, Path lfile, int postfix)
			throws RunRemoteCommandException, IOException, ScpException, JSchException, NoSuchAlgorithmException {
		Path localDir = lfile.getParent();
		if (!Files.exists(localDir)) {
			Files.createDirectories(localDir);
		}
		Path localTmpFile = localDir.resolve(lfile.getFileName().toString() + ".downloading");
		
		ScpUtil.from(session, rfile, localTmpFile.toString());
		
		String localMd5 = null;
		if (remoteMd5 != null) {
			localMd5 = Md5Checksum.getMD5Checksum(localTmpFile.toString());
		}
		
		if (localMd5 == null || remoteMd5.equalsIgnoreCase(localMd5)) {
			Path dst = lfile;
			if (postfix > 0) {
				dst = PathUtil.getNextAvailableByBaseName(lfile, postfix);
			} 
			return Files.move(localTmpFile, dst, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} else {
			throw new Md5ChecksumException();
		}
	}
	

	/**
	 * Download file with already known md5.
	 * @param session
	 * @param rfile
	 * @param remoteMd5
	 * @param lfile
	 * @return
	 * @throws RunRemoteCommandException
	 * @throws IOException
	 * @throws ScpException
	 * @throws JSchException
	 * @throws NoSuchAlgorithmException
	 */
	public static Path downloadWithTmpDownloadingFile(String os, Session session, String rfile,String remoteMd5, Path lfile)
			throws RunRemoteCommandException, IOException, ScpException, JSchException, NoSuchAlgorithmException {
		return downloadWithTmpDownloadingFileWithNewVersion(os, session, rfile, remoteMd5, lfile, -1);
	}

//	/**
//	 * Download file skip md5 check.
//	 * @param os
//	 * @param session
//	 * @param rfile
//	 * @param lfile
//	 * @return
//	 * @throws RunRemoteCommandException
//	 * @throws IOException
//	 * @throws ScpException
//	 * @throws JSchException
//	 * @throws NoSuchAlgorithmException
//	 */
//	public static Path downloadWithTmpDownloadingFile(String os, Session session, String rfile, Path lfile)
//			throws RunRemoteCommandException, IOException, ScpException, JSchException, NoSuchAlgorithmException {
//		return downloadWithTmpDownloadingFile(os, session, rfile, lfile, -1);
//	}
	
	public static boolean copy(String os, Session session, Path local, String remote) throws RunRemoteCommandException, JSchException, IOException {
		OsTypeWrapper otw = OsTypeWrapper.of(os);
		if (otw.isWin()) {
			try {
				ScpUtil.to(session, local.toString(), remote);
				return true;
			} catch (ScpException | IOException e) {
				String rdir = PathUtil.getParentWithoutEndingSeperator(remote);
				mkdirsp(os, session, rdir);
				try {
					ScpUtil.to(session, local.toString(), remote);
				} catch (ScpException | IOException e1) {
					return false;
				}
				return true;
			}
		} else {
			try {
				ScpUtil.to(session, local.toString(), remote);
				return true;
			} catch (ScpException | IOException e) {
				String rdir = PathUtil.getParentWithoutEndingSeperator(remote);
				mkdirsp(os, session, rdir);
				try {
					ScpUtil.to(session, local.toString(), remote);
				} catch (ScpException | IOException e1) {
					return false;
				}
				return true;
			}
		}
	}
	
	public static boolean copy(String os, Session session, String remote, byte[] bytes) throws RunRemoteCommandException, JSchException, IOException {
		OsTypeWrapper otw = OsTypeWrapper.of(os);
		if (otw.isWin()) {
			String rdir = PathUtil.getParentWithoutEndingSeperator(remote);
			if (!fileExists(os, session, rdir)) {
				mkdirsp(os, session, rdir);
			}
			try {
				ScpUtil.to(session, remote, bytes);
				return true;
			} catch (ScpException e) {
				return false;
			}
		} else {
			try {
				ScpUtil.to(session, remote, bytes);
				return true;
			} catch (ScpException e) {
				String rdir = PathUtil.getParentWithoutEndingSeperator(remote);
				mkdirsp(os, session, rdir);
				try {
					ScpUtil.to(session, remote, bytes);
				} catch (ScpException e1) {
					return false;
				}
				return true;
			}
		}
}
	
	/** find . -exec ls -ld {} \;
	 *  find . -print0 | xargs -0 ls -ld , this is much fast.
	 * @param session
	 * @param remoteFolder
	 * @return
	 * @throws IOException 
	 * @throws JSchException 
	 * @throws RunRemoteCommandException 
	 */
	
	public static List<LinuxLsl> listRemoteFilesRecursive(Session session, String remoteFolder) throws RunRemoteCommandException, JSchException, IOException {
		String command = String.format("find %s -print0 | xargs -0 ls -ld", remoteFolder);
		RemoteCommandResult rcr = runRemoteCommand(session, command);
		return rcr.getAllTrimedNotEmptyLines().stream().map(line -> LinuxLsl.matchAndReturnLinuxLsl(line)).filter(op -> op.isPresent()).map(op -> op.get()).collect(Collectors.toList());
	}
	
	public static List<LinuxLsl> listRemoteFiles(Session session, String remoteFolder) throws RunRemoteCommandException, JSchException, IOException {
		String command = String.format("ls -l %s", remoteFolder);
		RemoteCommandResult rcr = runRemoteCommand(session, command);
		return rcr.getAllTrimedNotEmptyLines().stream().map(line -> LinuxLsl.matchAndReturnLinuxLsl(line)).filter(op -> op.isPresent()).map(op -> op.get()).collect(Collectors.toList());
	}

	
//	/**
//	 * The localFolder and remoteFolder are the same base directory.For example, /local/a -> /remote/b
//	 * @param localFolder
//	 * @param remoteFolder
//	 * @return
//	 * @throws IOException 
//	 */
//	public static List<FileToCopyInfo> copyFolder(Session session, Path localFolder, String remoteFolder) throws IOException {
//		RemoteFileNotAbsoluteException.throwIfNeed(remoteFolder);
//		Path localFolderAbs = localFolder.toAbsolutePath();
//		String remoteFolderEndSlash = remoteFolder.endsWith("/") ? remoteFolder : remoteFolder + "/";
//		return Files.walk(localFolderAbs).map(path -> {
//			String rel = localFolderAbs.relativize(path).toString().replace('\\', '/');
//			String rfile = remoteFolderEndSlash + rel;
//			return new FileToCopyInfo(path, rfile, Files.isDirectory(path));
//		}).map(fi -> {
//			if (!fi.isDirectory()) {
//				try {
//					if (copy(session, fi.getLfileAbs(), fi.getRfileAbs())) {
//						fi.setDone(true);
//					} else {
//						fi.setDone(false);
//					}
//				} catch (RunRemoteCommandException | JSchException | IOException e) {
//					e.printStackTrace();
//				}
//			}
//			return fi;
//		}).collect(Collectors.toList());
//	}

	/**
	 * The localFolder and remoteFolder are the same base directory.For example, /local/a -> /remote/b
	 * @param localFolder
	 * @param remoteFolder
	 * @return
	 * @throws IOException 
	 */
	public static List<FileToCopyInfo> copyFolder(String os, Session session, Path localFolder, String remoteFolder) throws IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFolder);
		Path localFolderAbs = localFolder.toAbsolutePath();
		String remoteFolderEndSlash = remoteFolder.endsWith("/") ? remoteFolder : remoteFolder + "/";
		return Files.walk(localFolderAbs).map(path -> {
			String rel = localFolderAbs.relativize(path).toString().replace('\\', '/');
			String rfile = remoteFolderEndSlash + rel;
			return new FileToCopyInfo(path, rfile, Files.isDirectory(path));
		}).map(fi -> {
			if (!fi.isDirectory()) {
				try {
					if (copy(os, session, fi.getLfileAbs(), fi.getRfileAbs())) {
						fi.setDone(true);
					} else {
						fi.setDone(false);
					}
				} catch (RunRemoteCommandException | JSchException | IOException e) {
					e.printStackTrace();
				}
			}
			return fi;
		}).collect(Collectors.toList());
	}
	
	// @formatter:on

	/**
	 * returned backupfiles's backups list doesn't contain origin file.
	 * 
	 * @param session
	 * @param remoteFile
	 * @return
	 * @throws RunRemoteCommandException
	 * @throws IOException 
	 * @throws JSchException 
	 */
	public static BackupedFiles getRemoteBackupedFiles(Session session, String remoteFile)
			throws RunRemoteCommandException, JSchException, IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = new BackupedFiles(remoteFile);
		boolean isDirectory = isDirectory(session, remoteFile);
		List<String> fns;
		if (isDirectory) {
			String remoteFilep = PathUtil.getParentWithEndingSeparator(remoteFile);
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
	 * returned backupfiles's backups list doesn't contain origin file.
	 * 
	 * @param session
	 * @param remoteFile
	 * @return
	 * @throws RunRemoteCommandException
	 * @throws IOException 
	 * @throws JSchException 
	 */
	public static String getRemoteBackupedFilesWin(Session session, String remoteFile)
			throws RunRemoteCommandException, JSchException, IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
//		boolean isDirectory = isDirectoryWin(session, remoteFile);
		
		StringBuffer sb = new StringBuffer();
		sb.append("$f='").append(remoteFile).append("';$f + '*' |")
		.append("Get-ChildItem | ")
		.append("Foreach-Object {@{base=$_;dg=[int](Select-String -InputObject $_.Name -Pattern '(\\d*)$' -AllMatches).matches.groups[1].Value}} | ")
		.append("Sort-Object -Property @{Expression={$_.dg};Descending=$true} | ")
		.append("Select-Object -First 1 | ")
		.append("ForEach-Object {$f + '.' + ($_.dg + 1)} |")
		.append("ForEach-Object { if (Test-Path $f -Type Container) {Copy-Item -Path $f -Recurse -Destination $_} else {Copy-Item -Path $f -Destination $_}; $_}");

//		.append("Get-ChildItem -Path '%s*' | ")
//		.append("Foreach-Object {@{base=$_;dg=[int](Select-String -InputObject $_.Name -Pattern '(\\d*)$' -AllMatches).matches.groups[1].Value}} | ")
//		.append("Sort-Object -Property @{Expression={$_.dg};Descending=$true} | ")
//		.append("Select-Object -First 1 | ")
//		.append("ForEach-Object {'%s.' + ($_.dg + 1)} |")
//		.append("ForEach-Object {Copy-Item -Path %s %s -Destination $_; $_}");
		
		String command = sb.toString();
		
//		command = String.format(command, remoteFile, remoteFile, remoteFile, isDirectory ? "-Recurse" : "");
//		command = String.format(command, isDirectory ? "-Recurse" : "");
		
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, command);
		String v = rcr.getAllTrimedNotEmptyLines().get(0);
		return v;
		
	}

	/**
	 * Make one step back. for example, xx.2 -> xx, then delete xx.2.
	 * 
	 * @param session
	 * @param remoteFile
	 * @throws RunRemoteCommandException
	 * @throws IOException 
	 * @throws JSchException 
	 */
//	public static void revertFile(Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
//		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
//		BackupedFiles bfs = getRemoteBackupedFiles(session, remoteFile);
//		if (bfs.isOriginExists() && bfs.getNextInt() > 1) {
//			runRemoteCommand(session, String.format("cp %s %s", remoteFile + "." + (bfs.getNextInt() - 1), remoteFile));
//			deleteRemoteFile(session, remoteFile + "." + (bfs.getNextInt() - 1));
//		}
//	}
	
	public static void revertFile(String os, Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
		RemoteFileNotAbsoluteException.throwIfNeed(remoteFile);
		BackupedFiles bfs = getRemoteBackupedFiles(session, remoteFile);
		if (bfs.isOriginExists() && bfs.getNextInt() > 1) {
			runRemoteCommand(session, String.format("cp %s %s", remoteFile + "." + (bfs.getNextInt() - 1), remoteFile));
			deleteRemoteFile(os, session, remoteFile + "." + (bfs.getNextInt() - 1));
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
	
	public static int coreNumber(String os, Session session) throws JSchException, IOException {
		OsTypeWrapper otw = OsTypeWrapper.of(os);
		if (otw.isWin()) {
			String command = "Get-WmiObject win32_processor | Format-List -Property *";
			try {
				RemoteCommandResult rcr = runRemoteCommand(session, command);
				Map<String, String> mss = PSUtil.parseFormatList(rcr.getAllTrimedNotEmptyLines()).get(0); // LoadPercentage, NumberOfCores
				String k = "NumberOfCores";
				if(mss.containsKey(k)) {
					return StringUtil.parseInt(mss.get(k));
				} else {
					return -1;
				}
//				if (rcr.getExitValue() == 0) {
//					return Integer.valueOf(rcr.getStdOut().trim());
//				} else {
//					ExceptionUtil.logRemoteCommandResult(logger, rcr);
//					return -1;
//				}
			} catch (RunRemoteCommandException e) {
				ExceptionUtil.logErrorException(logger, e);
				return -1;
			}
			
//			ProcessExecResult pcr = PSUtil.runPsCommand(command);
//			Map<String, String> mss = PSUtil.parseFormatList(pcr.getStdOutFilterEmpty()).get(0); // LoadPercentage, NumberOfCores
//			return StringUtil.parseInt(mss.get("NumberOfCores"));
		} else {
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
	}

	public static int coreNumber(Session session) throws JSchException, IOException {
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
	
	public static RemoteCommandResult runRemoteCommand(Session session, String outCharset, String commandCharset, String command)
			throws JSchException, IOException {
		byte[] bytes;
		if (StringUtil.hasAnyNonBlankWord(commandCharset)) {
			bytes = command.getBytes(commandCharset);	
		} else {
			bytes = command.getBytes(StandardCharsets.UTF_8);
		}

		final Channel channel = session.openChannel("exec");
		try {
			((ChannelExec) channel).setCommand(bytes);
			channel.setInputStream(null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			((ChannelExec) channel).setErrStream(baos);
			InputStream in = channel.getInputStream();
			channel.connect();
			RemoteCommandResult cmdOut = SSHcommonUtil.readChannelOutput(channel, outCharset, in);
			String errOut = baos.toString();
			cmdOut.setErrOut(errOut);
			cmdOut.setCommand(command);
			return cmdOut;
		} finally {
			channel.disconnect();
		}
	}

	public static RemoteCommandResult runRemoteCommand(Session session, String charset, String command)
			throws JSchException, IOException {
		
		if (session == null) {
			logger.error("session is null when invoke remote command {}", command);
		}

		final Channel channel = session.openChannel("exec");
		try {
			((ChannelExec) channel).setCommand(command);

			channel.setInputStream(null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			((ChannelExec) channel).setErrStream(baos);
			InputStream in = channel.getInputStream();
			channel.connect();
			RemoteCommandResult cmdOut = SSHcommonUtil.readChannelOutput(channel, charset, in);
			String errOut = baos.toString();
			cmdOut.setErrOut(errOut);
			cmdOut.setCommand(command);
			return cmdOut;
		} finally {
			channel.disconnect();
		}
	}

	public static RemoteCommandResult runRemoteCommand(Session session, String command)
			throws RunRemoteCommandException, JSchException, IOException {
		return runRemoteCommand(session, null, command);
	}

//	public static void deleteRemoteFile(Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
//		runRemoteCommand(session, String.format("rm %s", remoteFile));
//	}
	
	public static void deleteRemoteFile(String os, Session session, String remoteFile) throws RunRemoteCommandException, JSchException, IOException {
		OsTypeWrapper otw = OsTypeWrapper.of(os);
		if (otw.isWin()) {
			runRemoteCommand(session, String.format("Remove-Item -Path %s -Force", remoteFile));
		} else {
			runRemoteCommand(session, String.format("rm %s", remoteFile));
		}
	}

	public static void deleteRemoteFolder(Session session, String remotectFolder) throws RunRemoteCommandException, JSchException, IOException {
		runRemoteCommand(session, String.format("rm -rf %s", remotectFolder));
	}

	public static void deleteRemoteFolder(String os, Session session, String remotectFolder) throws RunRemoteCommandException, JSchException, IOException {
		OsTypeWrapper otw = OsTypeWrapper.of(os);
		if (otw.isWin()) {
			runRemoteCommand(session, String.format("Remove-Item -Path %s -Recurse -Force", remotectFolder));
		} else {
			deleteRemoteFolder(session, remotectFolder);
		}
	}

	public static void deleteRemoteFile(Session session, List<String> remoteFiles) throws RunRemoteCommandException, JSchException, IOException {
		runRemoteCommand(session, String.format("rm %s", String.join(" ", remoteFiles)));
	}
	
	public static void deleteRemoteFile(String os, Session session, List<String> remoteFiles) throws RunRemoteCommandException, JSchException, IOException {
		OsTypeWrapper otw = OsTypeWrapper.of(os);
		if (otw.isWin()) {
			runRemoteCommand(session, String.format("Remove-Item -Path %s -Force", String.join(",", remoteFiles)));
		} else {
			runRemoteCommand(session, String.format("rm %s", String.join(" ", remoteFiles)));
		}
	}

	/**
	 * Test if all files exists on remote server.
	 * 
	 * @param session
	 * @param remoteFiles
	 * @return
	 * @throws RunRemoteCommandException
	 * @throws IOException 
	 * @throws JSchException 
	 */
	public static boolean allFileExists(Session session, String... remoteFiles) throws RunRemoteCommandException, JSchException, IOException {
		String cmd = String.format("ls -l %s", String.join(" ", remoteFiles));
		RemoteCommandResult rcr = runRemoteCommand(session, cmd);
		int v = rcr.getExitValue();
		return v == 0;
	}

//	public static boolean fileExists(Session session, String rfile) throws RunRemoteCommandException, JSchException, IOException {
//		List<String> lines = runRemoteCommand(session, String.format("ls %s", rfile)).getAllTrimedNotEmptyLines();
//		return !lines.stream().anyMatch(line -> line.indexOf("No such file or directory") != -1);
//	}

	public static boolean fileExists(String os, Session session, String rfile) throws RunRemoteCommandException, JSchException, IOException {
		OsTypeWrapper otw = OsTypeWrapper.of(os);
		if (otw.isWin()) {
			RemoteCommandResult rcr = runRemoteCommand(session, String.format("Get-Item %s", rfile));
			return rcr.getExitValue() == 0;
		} else {
			List<String> lines = runRemoteCommand(session, String.format("ls %s", rfile)).getAllTrimedNotEmptyLines();
			return !lines.stream().anyMatch(line -> line.indexOf("No such file or directory") != -1);
		}
	}

	public static RemoteCommandResult readChannelOutput(final Channel channel, String charset, InputStream in) throws IOException {
//		StringBuffer sb = new StringBuffer();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int exitValue = 0;
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				baos.write(tmp, 0, i);
//				sb.append(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				if (in.available() > 0)
					continue;
				exitValue = channel.getExitStatus();
				break;
			}
		}
		String s;
		if (StringUtil.hasAnyNonBlankWord(charset)) {
			s = baos.toString(charset);
		} else {
			s = baos.toString();
		}
		return RemoteCommandResult.partlyResult(s, exitValue);
	}

	public static RemoteCommandResult readChannelOutputDoBest(final Channel channel, String charset, InputStream in, String ptn)
			throws IOException {
		StringBuffer sb = new StringBuffer();
		String s = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int exitValue = 0;
		byte[] tmp = new byte[1024];
		long startTime = System.currentTimeMillis();
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				baos.write(tmp, 0, i);
//				sb.append(new String(tmp, 0, i));
				startTime = System.currentTimeMillis();
			}
			if (channel.isClosed()) {
				if (in.available() > 0)
					continue;
				exitValue = channel.getExitStatus();
				break;
			}
			Pattern pp = Pattern.compile(ptn, Pattern.DOTALL);
			
			
			if (StringUtil.hasAnyNonBlankWord(charset)) {
				s = baos.toString(charset);
			} else {
				s = baos.toString();
			}
			
			Matcher m = pp.matcher(s.trim());
			if (m.matches()) {
				break;
			}
			if ((System.currentTimeMillis() - startTime) > 3000) {
				break;
			}
		}
		return RemoteCommandResult.partlyResult(s, exitValue);
	}

}
