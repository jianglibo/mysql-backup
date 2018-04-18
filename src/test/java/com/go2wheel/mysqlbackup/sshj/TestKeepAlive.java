package com.go2wheel.mysqlbackup.sshj;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.YmlConfigFort;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

public class TestKeepAlive {
	
	@Test
	public void t() throws InterruptedException, IOException {
		YmlConfigFort c = UtilForTe.getYmlConfigFort();
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
        final SSHClient ssh = new SSHClient(defaultConfig);
        try {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(c.getSshHost());
            ssh.getConnection().getKeepAlive().setKeepAliveInterval(5); //every 60sec
            ssh.authPublickey("root", c.getSshIdrsa());
            Session session = ssh.startSession();
            session.allocateDefaultPTY();
            CountDownLatch cdl = new CountDownLatch(1);
            
            new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000 * 2);
						cdl.countDown();
					} catch (InterruptedException e) {
						cdl.countDown();
					}
				}
			}).run();;
            
            cdl.await();
            try {
                session.allocateDefaultPTY();
            } finally {
                session.close();
            }
        } finally {
            ssh.disconnect();
        }
	}

}
