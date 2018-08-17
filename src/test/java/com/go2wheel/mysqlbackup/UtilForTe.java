package com.go2wheel.mysqlbackup;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.go2wheel.mysqlbackup.MyAppSettings.SshConfig;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class UtilForTe {
	
	private static Logger logger = LoggerFactory.getLogger(UtilForTe.class);

	public static void printme(Object o) {
		System.out.println(o);
	}
	
	public static void deleteAllJobs(Scheduler scheduler) throws SchedulerException {
		try {
			for (JobKey jk : allJobs(scheduler)) {
				scheduler.deleteJob(jk);
			}
			;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static List<JobKey> allJobs(Scheduler scheduler) throws SchedulerException {
		List<JobKey> jks = new ArrayList<>();
		for (String groupName : scheduler.getJobGroupNames()) {
			jks.addAll(scheduler.getJobKeys(groupEquals(groupName)));
		}
		return jks;
	}
	
	public static MyAppSettings getMyAppSettings() throws IOException {
		try (InputStream is = ClassLoader.class.getResourceAsStream("/application-dev.properties")) {
			String s= StringUtil.inputstreamToString(is);
			printme(is);
			printme(s);
			printme(s.length());
		}
		
		try (InputStream is = ClassLoader.class.getResourceAsStream("/application-dev.properties")) {
			MyAppSettings mas = new MyAppSettings();
//			mas.setDataRoot(Paths.get("boxes"));
//			mas.setDownloadRoot(Paths.get("notingit"));
			Files.createDirectories(Paths.get("notingit"));
			Files.createDirectories(Paths.get("boxes"));
			SshConfig sc = new SshConfig();
			mas.setSsh(sc);
			if (is != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				String line = null;
				try {
					while((line = in.readLine()) != null) {
						printme(line);
						if (line.contains("sshIdrsa")) {
							sc.setSshIdrsa(line.split("=", 2)[1].trim());
						}
						
						if (line.contains("knownHosts")) {
							sc.setKnownHosts(line.split("=", 2)[1].trim());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				 
			}
			return mas;
		}
	}
	
	public static Server loadDemoBox() throws IOException {
		InputStream is =ClassLoader.class.getResourceAsStream("/demobox.yml");
		return YamlInstance.INSTANCE.yaml.loadAs(is, Server.class);
	}
	
	public static Path getMysqlInstanceDescription(String hostname) {
		return Paths.get("fixtures", "boxes", hostname, "description.yml");
	}

	public static Path getPathInThisProjectRelative(String fn) {
		Path currentRelativePath = Paths.get("").toAbsolutePath();
		return currentRelativePath.relativize(currentRelativePath.resolve(fn));
	}
	
	public static Path createTmpDirectory() throws IOException {
		return Files.createTempDirectory("tmpdirforpc");
	}
	
	public static Path createFileTree(String...fns) throws IOException {
		Path tmpFolder = Files.createTempDirectory("tmpfiletrees");
		for(String fn : fns) {
			String sanitized = fn.trim().replace('\\', '/');
			if (sanitized.startsWith("/")) {
				sanitized = sanitized.substring(1);
			}
			Path p = Paths.get(sanitized);
			String fileName = p.getFileName().toString();
			Path parent = p.getParent();
			Path brandNewDirectory;
			if (parent != null) {
				brandNewDirectory = Files.createDirectories(tmpFolder.resolve(parent));
			} else {
				brandNewDirectory = tmpFolder;
			}
			if (fileName.indexOf('.') != -1) { // it's a file.
				Files.write(brandNewDirectory.resolve(fileName), "hello".getBytes());
			} else {
				Files.createDirectories(brandNewDirectory.resolve(fileName));
			}
		}
		return tmpFolder;
	}

	
	public static String sshEcho(Session sshSession, String str) throws IOException, JSchException {
		final Channel channel = sshSession.openChannel("exec");
		try {
			((ChannelExec) channel).setCommand("echo " + str);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();

			RemoteCommandResult cmdOut = SSHcommonUtil.readChannelOutput(channel, in);
			assertThat(cmdOut.getStdOut().trim(), equalTo(str));
			assertThat("exit code should be 0.", cmdOut.getExitValue(), equalTo(0));
			return cmdOut.getStdOut();
		} finally {
			channel.disconnect();
		}
	}

}
