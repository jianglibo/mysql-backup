package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

import org.junit.Test;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 *  ssh-keyscan -H -t rsa 192.168.33.110
 * @author admin
 *
 */
public class TestKnownHosts extends SpringBaseFort {

	// @Test(expected = TransportException.class)
	// public void testNoKnownHostSetting() throws IOException {
	// new SSHClient().connect(box.getHost());
	// }

	@Test()
	public void testKnownHostFromFile() throws IOException, JSchException, RunRemoteCommandException {
		JSch jsch=new JSch();
		
		jsch.setKnownHosts(appSettings.getSsh().getKnownHosts());
		
		assertTrue(".known hosts should be exists.", Files.exists(Paths.get(appSettings.getSsh().getKnownHosts())));
		HostKeyRepository hkr = jsch.getHostKeyRepository();
		HostKey[] hks = hkr.getHostKey();
		HostKey demoKey = null;
		if (hks != null) {
			System.out.println("Host keys in " + hkr.getKnownHostsRepositoryID());
			for (int i = 0; i < hks.length; i++) {
				HostKey hk = hks[i];
				if (hk.getHost().equals(box.getHost())) {
					demoKey = hk;
				}
			}
		}
		assertNotNull(demoKey);
		System.out.println(demoKey.getType());
		System.out.println(demoKey.getFingerPrint(jsch));
		Session session=jsch.getSession(box.getUsername(), box.getHost(), box.getPort());
		session.setPassword(box.getPassword());
		session.connect();
		List<String> sl = SSHcommonUtil.runRemoteCommand(session, "ls -lh /tmp").getAllTrimedNotEmptyLines();
		
		assertThat(sl.size(), greaterThan(2));
		sl.stream().forEach(System.out::println);
	}
	
	@Test
	public void  tSecurityProvider() throws NoSuchAlgorithmException {
        final Provider[] providers = Security.getProviders();
        final String EOL = System.getProperty("line.separator");
        final Boolean verbose = Arrays.asList("").contains("-v");
        for (final Provider p : providers)
        {
            System.out.format("%s %s%s", p.getName(), p.getVersion(), EOL);
            for (final Object o : p.keySet())
            {
                if (verbose)
                {
                    System.out.format("\t%s : %s%s", o, p.getProperty((String)o), EOL);
                }
            }
        }
        
        boolean unlimited =
        	      Cipher.getMaxAllowedKeyLength("RC5") >= 256;
        	    System.out.println("Unlimited cryptography enabled: " + unlimited);
	}

	// @Test
	// public void testVerifier() throws IOException {
	// final SSHClient ssh = new SSHClient();
	// ssh.addHostKeyVerifier(new HostKeyVerifier() {
	// @Override
	// public boolean verify(String hostname, int port, PublicKey key) {
	// Path knownHosts = Paths.get(appSettings.getSsh().getKnownHosts());
	// assertTrue(Files.exists(knownHosts) && Files.isRegularFile(knownHosts));
	// return true;
	// }
	// });
	// ssh.connect(box.getHost());
	// executeEcho(ssh);
	// }
	//
	// // @Test
	// // ssh-keygen -lf ~/.ssh/id_rsa.pub, fingerprint is md5 of host's public key.
	// public void testFingerprint() throws IOException {
	// final SSHClient ssh = new SSHClient();
	// String fingerprintline =
	// Files.lines(Paths.get(appSettings.getSsh().getKnownHosts())).filter(line ->
	// line.indexOf(box.getHost()) != -1).findAny().get();
	// String[] splited = fingerprintline.split("\\s+");
	// assertThat("host fingerprint should has three columns.", splited.length,
	// equalTo(3));
	// ssh.addHostKeyVerifier("ecdsa-sha2-nistp256:" + splited[2]);
	// ssh.connect(box.getHost());
	// executeEcho(ssh);
	// }
	//
	// private void executeEcho(final SSHClient ssh)
	// throws UserAuthException, TransportException, ConnectionException,
	// IOException {
	// try {
	// ssh.authPublickey("root", appSettings.getSsh().getSshIdrsa());
	//
	// final Session session = ssh.startSession();
	// try {
	// final Command cmd = session.exec("echo 'abc'");
	// String cmdOut = IOUtils.readFully(cmd.getInputStream()).toString();
	// assertThat(cmdOut.trim(), equalTo("abc"));
	// cmd.join(5, TimeUnit.SECONDS);
	// assertThat("exit code should be 0.", cmd.getExitStatus(), equalTo(0));
	// } finally {
	// session.close();
	// }
	// } finally {
	// ssh.disconnect();
	// }
	// }
}
