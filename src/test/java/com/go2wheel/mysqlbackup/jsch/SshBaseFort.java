package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

public class SshBaseFort {

	protected Box demoBox;

	protected MyAppSettings appSettings;

	protected String TMP_SERVER_FILE_NAME = "/tmp/abc.txt";

	protected String TMP_SERVER_DIR_NAME = "/tmp/abc";

	protected String TMP_FILE_CONTENT = "abc";

	protected com.jcraft.jsch.Session sshSession;

	protected SshSessionFactory sshClientFactory;
	
	protected Path tmpDirectory;
	
	protected Path tmpFile;
	
	protected String remoteDemoFile;

	private long startTime;

	@Before
	public void before() throws IOException {
		doBefore();
	}
	
	private void doBefore() throws IOException {
		appSettings = UtilForTe.getMyAppSettings();
		sshClientFactory = new SshSessionFactory();
		sshClientFactory.setAppSettings(UtilForTe.getMyAppSettings());

		startTime = System.currentTimeMillis();
		if (!Files.exists(appSettings.getDataRoot())) {
			Files.createDirectories(appSettings.getDataRoot().resolve("demobox"));
		}
		demoBox = UtilForTe.loadDemoBox();
		sshSession = sshClientFactory.getConnectedSession(demoBox).orElse(null);
	}

	@After
	public void after() throws IOException, JSchException {
		if (tmpDirectory != null) {
			try {
				UtilForTe.deleteFolder(tmpDirectory);
			} catch (Exception e) {
			}
		}
		if (tmpFile != null) {
			Files.delete(tmpFile);
		}
		if (remoteDemoFile != null) {
			SSHcommonUtil.deleteRemoteFile(sshSession, remoteDemoFile);
		}
		
		if (sshSession != null) {
			sshSession.disconnect();
		}
	}

	protected void time() {
		System.out.println(String.format("time elapsed: %s ms", System.currentTimeMillis() - startTime));
	}

	protected void createAfileOnServer(String rfile, String content) throws IOException, JSchException {
		remoteDemoFile = rfile;
		final Channel channel = sshSession.openChannel("exec");
		try {
			((ChannelExec) channel).setCommand(String.format("echo %s > %s; cat %s", content,
					rfile, rfile));
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();

			RemoteCommandResult<String> cmdOut = SSHcommonUtil.readChannelOutput(channel, in);
			assertThat(cmdOut.getResult().trim(), equalTo(TMP_FILE_CONTENT));
			assertThat("exit code should be 0.", cmdOut.getExitValue(), equalTo(0));
		} finally {
			channel.disconnect();
		}
	}



	protected void createADirOnServer(int number) throws IOException, JSchException {
		final Channel channel = sshSession.openChannel("exec");
		StringBuilder sb = new StringBuilder(String.format("mkdir -p %s; rm -rf %s/*; mkdir %s/aabbcc",
				TMP_SERVER_DIR_NAME, TMP_SERVER_DIR_NAME, TMP_SERVER_DIR_NAME));
		for (int i = 0; i < number; i++) {
			sb.append(";");
			String s = String.format("echo %s > %s/%s", TMP_FILE_CONTENT, TMP_SERVER_DIR_NAME,
					"sshbasefile_" + i + ".txt");
			sb.append(s);
		}
		sb.append(";");
		String s = String.format("echo %s > %s/aabbcc/%s", TMP_FILE_CONTENT, TMP_SERVER_DIR_NAME, "sshbasefile_x.txt");
		sb.append(s);
		try {
			
			((ChannelExec) channel).setCommand(sb.toString());
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();

			RemoteCommandResult<String> cmdOut = SSHcommonUtil.readChannelOutput(channel, in);
			assertThat(cmdOut.getResult().trim(), equalTo(""));
			assertThat("exit code should be 0.", cmdOut.getExitValue(), equalTo(0));
		} finally {
			channel.disconnect();
		}
	}
	
	public void createALocalDir() throws IOException {
		tmpDirectory = Files.createTempDirectory("tmpfortest");
	}

	public void createALocalFile(String content) throws IOException {
		tmpFile = Files.createTempFile("sshbase", "txt");
		Files.write(tmpFile, content.getBytes());
	}

	public Path createALocalFileDirectory(int number) throws IOException {
		Path p = Files.createTempDirectory("sshbasedir");
		for (int i = 0; i < number; i++) {
			Path fp = p.resolve("sshbasefile_" + i + ".txt");
			Files.write(fp, TMP_FILE_CONTENT.getBytes());
		}
		Path nested = p.resolve("nested");
		Files.createDirectories(nested);
		Files.write(nested.resolve("a.txt"), TMP_FILE_CONTENT.getBytes());

		return p;
	}

	public void assertDirectory(Path topPath, long dirs, long files, long total) throws IOException {
		assertThat("directories should right.", Files.list(topPath).filter(Files::isDirectory).count(), equalTo(dirs));
		assertThat("files should right.", Files.list(topPath).filter(Files::isRegularFile).count(), equalTo(files));
		assertThat("total should right.", Files.walk(topPath).count(), equalTo(total));

	}

}
