package com.go2wheel.mysqlbackup;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.go2wheel.mysqlbackup.MyAppSettings.SshConfig;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class UtilForTe {
	
	private static Pattern getItemPtn(String name) {
		return Pattern.compile("\\s+" + name + ":\\s*(.*?)\\s*");
	}

	public static void printme(Object o) {
		System.out.println(o);
	}
	
	public static MyAppSettings getMyAppSettings() throws IOException {
		InputStream is =ClassLoader.class.getResourceAsStream("/application.yml");
		
		MyAppSettings mas = new MyAppSettings();
		mas.setDataRoot(Paths.get("boxes"));
		mas.setDownloadRoot(Paths.get("notingit"));
		Files.createDirectories(Paths.get("notingit"));
		Files.createDirectories(Paths.get("boxes"));
		SshConfig sc = new SshConfig();
		mas.setSsh(sc);
		if (is != null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			try {
				while((line = in.readLine()) != null) {
					Matcher m = getItemPtn("sshIdrsa").matcher(line);
					if (m.matches()) {
						 sc.setSshIdrsa((String) m.group(1));
					}
					m = getItemPtn("knownHosts").matcher(line);
					if (m.matches()) {
						sc.setKnownHosts((m.group(1)));
					}
				}
			} catch (IOException e) {
			}
			 
		}
		return mas;
	}
	
	public static Box loadDemoBox() throws IOException {
		InputStream is =ClassLoader.class.getResourceAsStream("/demobox.yml");
		return YamlInstance.INSTANCE.getYaml().loadAs(is, Box.class);
	}
	
	public static BackupCommand backupCommandInstance() throws IOException {
		BackupCommand bc = new BackupCommand();
		bc.setInstancesBase(Files.createTempDirectory("backupcommandbase"));
		return bc;
	}
	
	
	public static Path getMysqlInstanceDescription(String hostname) {
		return Paths.get("fixtures", "boxes", hostname, "description.yml");
	}

	public static Path getPathInThisProjectRelative(String fn) {
		Path currentRelativePath = Paths.get("").toAbsolutePath();
		return currentRelativePath.relativize(currentRelativePath.resolve(fn));
	}
	
	public static void deleteFolder(Path folder) throws IOException {
		if (folder == null || !Files.exists(folder)) {
			return;
		}
		
		if (Files.isRegularFile(folder)) {
			Files.delete(folder);
		}
		Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
		    @Override
		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		        throws IOException
		    {
		    	Files.delete(file);
		        return FileVisitResult.CONTINUE;
		    }
	    @Override
	    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
	        throws IOException
	    {
	    	Files.delete(dir);
	        if (exc != null)
	            throw exc;
	        return FileVisitResult.CONTINUE;
	    }
		});
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

			RemoteCommandResult<String> cmdOut = SSHcommonUtil.readChannelOutput(channel, in);
			assertThat(cmdOut.getResult().trim(), equalTo(str));
			assertThat("exit code should be 0.", cmdOut.getExitValue(), equalTo(0));
			return cmdOut.getResult();
		} finally {
			channel.disconnect();
		}
	}

}
