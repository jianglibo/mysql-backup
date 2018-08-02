package com.go2wheel.mysqlbackup.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.go2wheel.mysqlbackup.exception.Md5ChecksumException;

/**
 * copy from https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
 * @author jianglibo@gmail.com
 *
 */
public class Md5Checksum {
	   public static byte[] createChecksum(String filename) throws NoSuchAlgorithmException, IOException {
	       try (InputStream fis =  new FileInputStream(filename)) {
			   byte[] buffer = new byte[1024];
			   MessageDigest complete = MessageDigest.getInstance("MD5");
			   int numRead;

			   do {
			       numRead = fis.read(buffer);
			       if (numRead > 0) {
			           complete.update(buffer, 0, numRead);
			       }
			   } while (numRead != -1);

			   fis.close();
			   return complete.digest();
		}
//			   catch (NoSuchAlgorithmException | IOException e) {
//			Md5ChecksumException me = new Md5ChecksumException();
//			me.initCause(e);
//			throw me;
//		}
	   }

	   // see this How-to for a faster way to convert
	   // a byte array to a HEX string
	   public static String getMD5Checksum(String filename) throws NoSuchAlgorithmException, IOException {
	       byte[] b = createChecksum(filename);
	       String result = "";

	       for (int i=0; i < b.length; i++) {
	           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
	       }
	       return result;
	   }
	   
	   public static String getMD5Checksum(Path file) throws NoSuchAlgorithmException, IOException {
		   return getMD5Checksum(file.toAbsolutePath().toString());
	   }

}
