package com.go2wheel.mysqlbackup.sshj;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.util.SshClientFactory;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

public class SshBaseFort {
	
	protected MysqlInstance demoInstance;
	
	protected MyAppSettings appSettings;
	
	protected String TMP_SERVER_FILE_NAME = "/tmp/abc.txt";
	
	protected String TMP_SERVER_DIR_NAME = "/tmp/abc";
	
	protected String TMP_FILE_CONTENT = "abc";
	
	protected SSHClient sshClient;
	
	private long startTime;
	
	@Before
	public void before() throws IOException {
		demoInstance = UtilForTe.loadDemoInstance();
		appSettings = UtilForTe.getMyAppSettings();
		SshClientFactory scf = new SshClientFactory();
		scf.setAppSettings(UtilForTe.getMyAppSettings());
		sshClient = scf.getConnectedSSHClient(demoInstance).get();
		startTime = System.currentTimeMillis();
	}

	@After
	public void after() {
		try {
			sshClient.disconnect();
		} catch (IOException e) {
		}
	}
	
	protected void time() {
		System.out.println(String.format("time elapsed: %s ms", System.currentTimeMillis() - startTime));
	}
	
	protected void createAfileOnServer() throws IOException {
		final Session session = sshClient.startSession();
		try {
			final Command cmd = session.exec(String.format("echo %s > %s; cat %s",TMP_FILE_CONTENT, TMP_SERVER_FILE_NAME, TMP_SERVER_FILE_NAME));
			String cmdOut = IOUtils.readFully(cmd.getInputStream()).toString();
			assertThat(cmdOut.trim(), equalTo(TMP_FILE_CONTENT));
			assertThat("exit code should be 0.", cmd.getExitStatus(), equalTo(0));
		} finally {
			session.close();
		}
	}
	
	protected void createADirOnServer(int number) throws IOException {
		final Session session = sshClient.startSession();
		StringBuilder sb = new StringBuilder(String.format("mkdir -p %s; rm -rf %s/*; mkdir %s/aabbcc", TMP_SERVER_DIR_NAME, TMP_SERVER_DIR_NAME, TMP_SERVER_DIR_NAME));
		for(int i =0 ; i< number; i++) {
			sb.append(";");
			String s = String.format("echo %s > %s/%s",TMP_FILE_CONTENT, TMP_SERVER_DIR_NAME, "sshbasefile_" + i + ".txt");
			sb.append(s);
		}
		sb.append(";");
		String s = String.format("echo %s > %s/aabbcc/%s",TMP_FILE_CONTENT, TMP_SERVER_DIR_NAME, "sshbasefile_x.txt");
		sb.append(s);
		try {
			final Command cmd = session.exec(sb.toString());
			String cmdOut = IOUtils.readFully(cmd.getInputStream()).toString();
			assertThat(cmdOut.trim(), equalTo(""));
			assertThat("exit code should be 0.", cmd.getExitStatus(), equalTo(0));
		} finally {
			session.close();
		}
	}
	
	public Path createALocalFile() throws IOException {
		Path p = Files.createTempFile("sshbase", "txt");
		Files.write(p, TMP_FILE_CONTENT.getBytes());
		return p;
	}
	
	public Path createALocalFileDirectory(int number) throws IOException {
		Path p = Files.createTempDirectory("sshbasedir");
		for(int i =0 ; i< number; i++) {
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
