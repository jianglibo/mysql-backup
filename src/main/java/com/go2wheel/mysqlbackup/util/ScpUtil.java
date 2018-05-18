package com.go2wheel.mysqlbackup.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.go2wheel.mysqlbackup.exception.ScpException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ScpUtil {

	protected static void to(Session session, String rfile, InputStream is, long contentLength, String maybeFileName)
			throws ScpException {

		try { 
			boolean ptimestamp = false;
			String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			// get I/O streams for remote scp
			try(OutputStream out = channel.getOutputStream();
					InputStream in = channel.getInputStream();) {
				channel.connect();
				if (ScpUtil.checkAck(in) != 0) {
					throw new ScpException("", rfile, "ACK error.");
				}

				long filesize = contentLength;
				if (maybeFileName == null || maybeFileName.trim().isEmpty()) {
					maybeFileName = "afilename";
				}
				command = "C0644 " + filesize + " " + maybeFileName + "\n";
				out.write(command.getBytes());
				out.flush();
				if (ScpUtil.checkAck(in) != 0) {
					throw new ScpException("", rfile, "ACK error.");
				}

				// send a content of lfile
				// fis = new FileInputStream(lfile);
				byte[] buf = new byte[1024];
				while (true) {
					int len = is.read(buf, 0, buf.length);
					if (len <= 0)
						break;
					out.write(buf, 0, len); // out.flush();
				}
				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
				if (ScpUtil.checkAck(in) != 0) {
					throw new ScpException("", rfile, "ACK error.");
				}
				channel.disconnect();
			}
		} catch (JSchException | IOException e) {
			throw new ScpException("", rfile, e.getMessage());
		}
	}

	public static void to(Session session, String lfile, String rfile) throws ScpException, IOException {
		Path lpath = Paths.get(lfile);
		try (InputStream is = Files.newInputStream(lpath)) {
			to(session, rfile, is, Files.size(lpath), lpath.getFileName().toString());
		}
	}

	public static void to(Session session, String rfile, byte[] content) throws ScpException {
		Path rpath = Paths.get(rfile);
		to(session, rfile, new ByteArrayInputStream(content), content.length, rpath.getFileName().toString());
	}

	public static void from(Session session, String rfile, OutputStream os)
			throws JSchException, IOException, ScpException {
		// exec 'scp -f rfile' remotely
		String command = "scp -f " + rfile;
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);

		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();

		channel.connect();

		byte[] buf = new byte[1024];

		// send '\0'
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();

		while (true) {
			int c = ScpUtil.checkAck(in);
			if (c != 'C') {
				break;
			}

			// read '0644 '
			in.read(buf, 0, 5);

			long filesize = 0L;
			while (true) {
				if (in.read(buf, 0, 1) < 0) {
					// error
					break;
				}
				if (buf[0] == ' ')
					break;
				filesize = filesize * 10L + (long) (buf[0] - '0');
			}

			@SuppressWarnings("unused")
			String file = null;
			for (int i = 0;; i++) {
				in.read(buf, i, 1);
				if (buf[i] == (byte) 0x0a) {
					file = new String(buf, 0, i);
					break;
				}
			}
			// System.out.println("filesize="+filesize+", file="+file);
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			// read a content of lfile
			// fos = new FileOutputStream(prefix == null ? lfile : prefix + file);

			int foo;
			while (true) {
				if (buf.length < filesize)
					foo = buf.length;
				else
					foo = (int) filesize;
				foo = in.read(buf, 0, foo);
				if (foo < 0) {
					// error
					break;
				}
				os.write(buf, 0, foo);
				filesize -= foo;
				if (filesize == 0L)
					break;
			}
			if (ScpUtil.checkAck(in) != 0) {
				throw new ScpException(rfile, "", "");
			}

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
		}
	}

	public static Path from(Session session, String rfile, String lfile) throws ScpException {
		try {
			Path lpath = Paths.get(lfile);
			Path rpath = Paths.get(rfile);

			if (Files.isDirectory(lpath)) {
				lpath = lpath.resolve(rpath.getFileName());
			}
			try (OutputStream os = Files.newOutputStream(lpath)) {
				from(session, rfile, os);
			}
			return lpath;
		} catch (Exception e) {
			throw new ScpException(rfile, lfile, "");
		}
	}

	public static ByteArrayOutputStream from(Session session, String rfile)
			throws JSchException, IOException, ScpException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		from(session, rfile, os);
		return os;
	}

	// public static void from(Session session, String rfile, String lfile) {
	// FileOutputStream fos = null;
	// try {
	// String prefix = null;
	// if (new File(lfile).isDirectory()) {
	// prefix = lfile + File.separator;
	// }
	// // exec 'scp -f rfile' remotely
	// String command = "scp -f " + rfile;
	// Channel channel = session.openChannel("exec");
	// ((ChannelExec) channel).setCommand(command);
	//
	// // get I/O streams for remote scp
	// OutputStream out = channel.getOutputStream();
	// InputStream in = channel.getInputStream();
	//
	// channel.connect();
	//
	// byte[] buf = new byte[1024];
	//
	// // send '\0'
	// buf[0] = 0;
	// out.write(buf, 0, 1);
	// out.flush();
	//
	// while (true) {
	// int c = ScpUtil.checkAck(in);
	// if (c != 'C') {
	// break;
	// }
	//
	// // read '0644 '
	// in.read(buf, 0, 5);
	//
	// long filesize = 0L;
	// while (true) {
	// if (in.read(buf, 0, 1) < 0) {
	// // error
	// break;
	// }
	// if (buf[0] == ' ')
	// break;
	// filesize = filesize * 10L + (long) (buf[0] - '0');
	// }
	//
	// String file = null;
	// for (int i = 0;; i++) {
	// in.read(buf, i, 1);
	// if (buf[i] == (byte) 0x0a) {
	// file = new String(buf, 0, i);
	// break;
	// }
	// }
	//
	// // System.out.println("filesize="+filesize+", file="+file);
	//
	// // send '\0'
	// buf[0] = 0;
	// out.write(buf, 0, 1);
	// out.flush();
	//
	// // read a content of lfile
	// fos = new FileOutputStream(prefix == null ? lfile : prefix + file);
	// int foo;
	// while (true) {
	// if (buf.length < filesize)
	// foo = buf.length;
	// else
	// foo = (int) filesize;
	// foo = in.read(buf, 0, foo);
	// if (foo < 0) {
	// // error
	// break;
	// }
	// fos.write(buf, 0, foo);
	// filesize -= foo;
	// if (filesize == 0L)
	// break;
	// }
	// fos.close();
	// fos = null;
	//
	// if (ScpUtil.checkAck(in) != 0) {
	// throw new ScpFromException(rfile, lfile);
	// }
	//
	// // send '\0'
	// buf[0] = 0;
	// out.write(buf, 0, 1);
	// out.flush();
	// }
	// } catch (Exception e) {
	// try {
	// if (fos != null)
	// fos.close();
	// } catch (Exception ee) {
	// }
	// throw new ScpFromException(rfile, lfile);
	// }
	// }

	public static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				// System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				// System.out.print(sb.toString());
			}
		}
		return b;
	}

	// public static void to(Session session, String lfile, String rfile) {
	//
	// FileInputStream fis = null;
	// try {
	// boolean ptimestamp = false;
	// String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + rfile;
	// Channel channel = session.openChannel("exec");
	// ((ChannelExec) channel).setCommand(command);
	// // get I/O streams for remote scp
	// OutputStream out = channel.getOutputStream();
	// InputStream in = channel.getInputStream();
	// channel.connect();
	// if (ScpUtil.checkAck(in) != 0) {
	// throw new ScpToException(lfile, rfile);
	// }
	//
	// File _lfile = new File(lfile);
	//
	// if (ptimestamp) {
	// command = "T " + (_lfile.lastModified() / 1000) + " 0";
	// // The access time should be sent here,
	// // but it is not accessible with JavaAPI ;-<
	// command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
	// out.write(command.getBytes());
	// out.flush();
	// if (ScpUtil.checkAck(in) != 0) {
	// throw new ScpToException(lfile, rfile);
	// }
	// }
	//
	// // send "C0644 filesize filename", where filename should not include '/'
	// long filesize = _lfile.length();
	// command = "C0644 " + filesize + " ";
	// if (lfile.lastIndexOf('/') > 0) {
	// command += lfile.substring(lfile.lastIndexOf('/') + 1);
	// } else {
	// command += lfile;
	// }
	// command += "\n";
	// out.write(command.getBytes());
	// out.flush();
	// if (ScpUtil.checkAck(in) != 0) {
	// throw new ScpToException(lfile, rfile);
	// }
	//
	// // send a content of lfile
	// fis = new FileInputStream(lfile);
	// byte[] buf = new byte[1024];
	// while (true) {
	// int len = fis.read(buf, 0, buf.length);
	// if (len <= 0)
	// break;
	// out.write(buf, 0, len); // out.flush();
	// }
	// fis.close();
	// fis = null;
	// // send '\0'
	// buf[0] = 0;
	// out.write(buf, 0, 1);
	// out.flush();
	// if (ScpUtil.checkAck(in) != 0) {
	// throw new ScpToException(lfile, rfile);
	// }
	// out.close();
	// channel.disconnect();
	// } catch (Exception e) {
	// try {
	// if (fis != null)
	// fis.close();
	// } catch (Exception ee) {
	// }
	// throw new ScpToException(lfile, rfile);
	// }
	// }

}
