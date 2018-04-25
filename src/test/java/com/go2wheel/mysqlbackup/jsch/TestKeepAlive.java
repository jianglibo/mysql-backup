package com.go2wheel.mysqlbackup.jsch;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class TestKeepAlive extends SshBaseFort {
	
//	@Test
//	public void t() throws InterruptedException, IOException {
//
//        DefaultConfig defaultConfig = new DefaultConfig();
//        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
//        final SSHClient ssh = new SSHClient(defaultConfig);
//        try {
//            ssh.addHostKeyVerifier(new PromiscuousVerifier());
//            ssh.connect(demoBox.getHost());
//            ssh.getConnection().getKeepAlive().setKeepAliveInterval(5); //every 60sec
//            ssh.authPublickey("root", appSettings.getSsh().getSshIdrsa());
//            Session session = ssh.startSession();
////            session.allocateDefaultPTY();
//            CountDownLatch cdl = new CountDownLatch(1);
//            
//            new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						Thread.sleep(1000 * 2);
//						cdl.countDown();
//					} catch (InterruptedException e) {
//						cdl.countDown();
//					}
//				}
//			}).run();;
//            
//            cdl.await();
//            try {
//                session.allocateDefaultPTY();
//            } finally {
//                session.close();
//            }
//        } finally {
//            ssh.disconnect();
//        }
//	}

}
