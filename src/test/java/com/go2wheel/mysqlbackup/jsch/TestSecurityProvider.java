package com.go2wheel.mysqlbackup.jsch;

import static org.junit.Assert.assertNotNull;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import javax.crypto.Cipher;

import org.junit.Test;

public class TestSecurityProvider {

	
	
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
	
	@Test
	public void tKeygen() throws ClassNotFoundException, SQLException {
	    Class.forName("org.hsqldb.jdbc.JDBCDriver");
	    Connection con = DriverManager.getConnection("jdbc:hsqldb:mem:abc", "SA", "");
	    Statement stmt = con.createStatement();  
	    ResultSet rs = stmt.executeQuery("CALL CRYPT_KEY('AES', null);");
	    rs.next();
	    String key1 = rs.getString(1);
	    assertNotNull(key1);
	}
}
