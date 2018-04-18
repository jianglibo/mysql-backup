package com.go2wheel.mysqlbackup.sshj;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.YmlConfigFort;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

public class TestExec {
	
	private YmlConfigFort c = UtilForTe.getYmlConfigFort();

	@Test
	public void t() throws IOException {
		Assume.assumeTrue(c.isEnvExists());
        final SSHClient ssh = new SSHClient();
        Path knownHosts = Paths.get(c.getKnownHosts()); 
        assertTrue(Files.exists(knownHosts) && Files.isRegularFile(knownHosts));

        ssh.loadKnownHosts(knownHosts.toFile());
        ssh.connect(c.getSshHost());
        try {
//            ssh.authPublickey("root");
            ssh.authPublickey("root", c.getSshIdrsa());
            
            final Session session = ssh.startSession();
            try {
                final Command cmd = session.exec("ping -c 1 bing.com");
                System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
                cmd.join(5, TimeUnit.SECONDS);
                System.out.println("\n** exit status: " + cmd.getExitStatus());
            } finally {
                session.close();
            }
        } finally {
            ssh.disconnect();
        }
	}
}
