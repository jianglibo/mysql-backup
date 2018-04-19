package com.go2wheel.mysqlbackup.sshj;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.YmlConfigFort;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;

public class TestKnownHosts {

	private YmlConfigFort c = UtilForTe.getYmlConfigFort();
	
	@Test(expected = TransportException.class)
	public void testNoKnownHostSetting() throws IOException {
		Assume.assumeTrue(c.isEnvExists());
		new SSHClient().connect(c.getSshHost());
	}
	
	@Test()
	public void testKnownHostFromFile() throws IOException {
		Assume.assumeTrue(c.isEnvExists());
		final SSHClient ssh = new SSHClient();
		Path knownHosts = Paths.get(c.getKnownHosts());
		assertTrue(Files.exists(knownHosts) && Files.isRegularFile(knownHosts));
		ssh.loadKnownHosts(knownHosts.toFile());
		ssh.connect(c.getSshHost());
		executeEcho(ssh);
	}
	
	@Test
	public void testVerifier() throws IOException {
		Assume.assumeTrue(c.isEnvExists());
		final SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new HostKeyVerifier() {
			@Override
			public boolean verify(String hostname, int port, PublicKey key) {
				Path knownHosts = Paths.get(c.getKnownHosts());
				assertTrue(Files.exists(knownHosts) && Files.isRegularFile(knownHosts));
				return true;
			}
		});
		ssh.connect(c.getSshHost());
		executeEcho(ssh);
	}
	
//	@Test
	// ssh-keygen -lf ~/.ssh/id_rsa.pub, fingerprint is md5 of host's public key.
	public void testFingerprint() throws IOException {
		Assume.assumeTrue(c.isEnvExists());
		final SSHClient ssh = new SSHClient();
		String fingerprintline = Files.lines(Paths.get(c.getKnownHosts())).filter(line -> line.indexOf(c.getSshHost()) != -1).findAny().get();
		String[] splited = fingerprintline.split("\\s+");
		assertThat("host fingerprint should has three columns.", splited.length, equalTo(3));
		ssh.addHostKeyVerifier("ecdsa-sha2-nistp256:" + splited[2]);
		ssh.connect(c.getSshHost());
		executeEcho(ssh);
	}

	private void executeEcho(final SSHClient ssh)
			throws UserAuthException, TransportException, ConnectionException, IOException {
		try {
			ssh.authPublickey("root", c.getSshIdrsa());

			final Session session = ssh.startSession();
			try {
				final Command cmd = session.exec("echo 'abc'");
				String cmdOut = IOUtils.readFully(cmd.getInputStream()).toString();
				assertThat(cmdOut.trim(), equalTo("abc"));
				cmd.join(5, TimeUnit.SECONDS);
				assertThat("exit code should be 0.", cmd.getExitStatus(), equalTo(0));
			} finally {
				session.close();
			}
		} finally {
			ssh.disconnect();
		}
	}
}