package com.go2wheel.mysqlbackup.expect;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static net.sf.expectit.matcher.Matchers.anyString;
import static net.sf.expectit.matcher.Matchers.exact;
import static net.sf.expectit.matcher.Matchers.eof;
import static net.sf.expectit.matcher.Matchers.times;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.ExpectitUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.filter.Filter;
import net.sf.expectit.filter.FilterAdapter;

public class TestGetMysqlCnf extends SshBaseFort {
	
	/**
	 * .*foo  // greedy quantifier
		Enter input string to search: xfooxxxxxxfoo
		I found the text "xfooxxxxxxfoo" starting at index 0 and ending at index 13.
		
		.*?foo  // reluctant quantifier
		Enter input string to search: xfooxxxxxxfoo
		I found the text "xfoo" starting at index 0 and ending at index 4.
		I found the text "xxxxxxfoo" starting at index 4 and ending at index 13.
		
		.*+foo // possessive quantifier
		Enter input string to search: xfooxxxxxxfoo
		No match found.
	 */
	@Test
	public void tr() {
		String s = "[root@localhost ~]# mysql --help --verbose\r\n" + 
				"mysql  Ver 14.14 Distrib 5.6.40, for Linux (x86_64) using  EditLine wrapper\r\n" + 
				"Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.\r\n" + 
				"\r\n" + 
				"Oracle is a registered trademark of Oracle Corporation and/or its\r\n" + 
				"affiliates. Other names may be trademarks of their respective\r\n" + 
				"owners.\r\n" + 
				"\r\n" + 
				"Usage: mysql [OPTIONS] [database]\r\n" + 
				"  -?, --help          Display this help and exit.\r\n" + 
				"  -I, --help          Synonym for -?\r\n" + 
				"  --auto-rehash       Enable automatic rehashing. One doesn't need to use\r\n" + 
				"                      'rehash' to get table and field completion, but startup\r\n" + 
				"                      and reconnecting may take a longer time. Disable with\r\n" + 
				"                      --disable-auto-rehash.\r\n" + 
				"                      (Defaults to on; use --skip-auto-rehash to disable.)\r\n" + 
				"  -A, --no-auto-rehash \r\n" + 
				"                      No automatic rehashing. One has to use 'rehash' to get\r\n" + 
				"                      table and field completion. This gives a quicker start of\r\n" + 
				"                      mysql and disables rehashing on reconnect.\r\n" + 
				"  --auto-vertical-output \r\n" + 
				"                      Automatically switch to vertical output mode if the\r\n" + 
				"                      result is wider than the terminal width.\r\n" + 
				"  -B, --batch         Don't use history file. Disable interactive behavior.\r\n" + 
				"                      (Enables --silent.)\r\n" + 
				"  --bind-address=name IP address to bind to.\r\n" + 
				"  -b, --binary-as-hex Print binary data as hex\r\n" + 
				"  --character-sets-dir=name \r\n" + 
				"                      Directory for character set files.\r\n" + 
				"  --column-type-info  Display column type information.\r\n" + 
				"  -c, --comments      Preserve comments. Send comments to the server. The\r\n" + 
				"                      default is --skip-comments (discard comments), enable\r\n" + 
				"                      with --comments.\r\n" + 
				"  -C, --compress      Use compression in server/client protocol.\r\n" + 
				"  -#, --debug[=#]     This is a non-debug version. Catch this and exit.\r\n" + 
				"  --debug-check       Check memory and open file usage at exit.\r\n" + 
				"  -T, --debug-info    Print some debug info at exit.\r\n" + 
				"  -D, --database=name Database to use.\r\n" + 
				"  --default-character-set=name \r\n" + 
				"                      Set the default character set.\r\n" + 
				"  --delimiter=name    Delimiter to be used.\r\n" + 
				"  --enable-cleartext-plugin \r\n" + 
				"                      Enable/disable the clear text authentication plugin.\r\n" + 
				"  -e, --execute=name  Execute command and quit. (Disables --force and history\r\n" + 
				"                      file.)\r\n" + 
				"  -E, --vertical      Print the output of a query (rows) vertically.\r\n" + 
				"  -f, --force         Continue even if we get an SQL error.\r\n" + 
				"  -G, --named-commands \r\n" + 
				"                      Enable named commands. Named commands mean this program's\r\n" + 
				"                      internal commands; see mysql> help . When enabled, the\r\n" + 
				"                      named commands can be used from any line of the query,\r\n" + 
				"                      otherwise only from the first line, before an enter.\r\n" + 
				"                      Disable with --disable-named-commands. This option is\r\n" + 
				"                      disabled by default.\r\n" + 
				"  -i, --ignore-spaces Ignore space after function names.\r\n" + 
				"  --init-command=name SQL Command to execute when connecting to MySQL server.\r\n" + 
				"                      Will automatically be re-executed when reconnecting.\r\n" + 
				"  --local-infile      Enable/disable LOAD DATA LOCAL INFILE.\r\n" + 
				"  -b, --no-beep       Turn off beep on error.\r\n" + 
				"  -h, --host=name     Connect to host.\r\n" + 
				"  -H, --html          Produce HTML output.\r\n" + 
				"  -X, --xml           Produce XML output.\r\n" + 
				"  --line-numbers      Write line numbers for errors.\r\n" + 
				"                      (Defaults to on; use --skip-line-numbers to disable.)\r\n" + 
				"  -L, --skip-line-numbers \r\n" + 
				"                      Don't write line number for errors.\r\n" + 
				"  -n, --unbuffered    Flush buffer after each query.\r\n" + 
				"  --column-names      Write column names in results.\r\n" + 
				"                      (Defaults to on; use --skip-column-names to disable.)\r\n" + 
				"  -N, --skip-column-names \r\n" + 
				"                      Don't write column names in results.\r\n" + 
				"  --sigint-ignore     Ignore SIGINT (CTRL-C).\r\n" + 
				"  -o, --one-database  Ignore statements except those that occur while the\r\n" + 
				"                      default database is the one named at the command line.\r\n" + 
				"  --pager[=name]      Pager to use to display results. If you don't supply an\r\n" + 
				"                      option, the default pager is taken from your ENV variable\r\n" + 
				"                      PAGER. Valid pagers are less, more, cat [> filename],\r\n" + 
				"                      etc. See interactive help (\\h) also. This option does not\r\n" + 
				"                      work in batch mode. Disable with --disable-pager. This\r\n" + 
				"                      option is disabled by default.\r\n" + 
				"  -p, --password[=name] \r\n" + 
				"                      Password to use when connecting to server. If password is\r\n" + 
				"                      not given it's asked from the tty.\r\n" + 
				"  -P, --port=#        Port number to use for connection or 0 for default to, in\r\n" + 
				"                      order of preference, my.cnf, $MYSQL_TCP_PORT,\r\n" + 
				"                      /etc/services, built-in default (3306).\r\n" + 
				"  --prompt=name       Set the mysql prompt to this value.\r\n" + 
				"  --protocol=name     The protocol to use for connection (tcp, socket, pipe,\r\n" + 
				"                      memory).\r\n" + 
				"  -q, --quick         Don't cache result, print it row by row. This may slow\r\n" + 
				"                      down the server if the output is suspended. Doesn't use\r\n" + 
				"                      history file.\r\n" + 
				"  -r, --raw           Write fields without conversion. Used with --batch.\r\n" + 
				"  --reconnect         Reconnect if the connection is lost. Disable with\r\n" + 
				"                      --disable-reconnect. This option is enabled by default.\r\n" + 
				"                      (Defaults to on; use --skip-reconnect to disable.)\r\n" + 
				"  -s, --silent        Be more silent. Print results with a tab as separator,\r\n" + 
				"                      each row on new line.\r\n" + 
				"  -S, --socket=name   The socket file to use for connection.\r\n" + 
				"  --ssl               Enable SSL for connection (automatically enabled with\r\n" + 
				"                      other flags).\r\n" + 
				"  --ssl-ca=name       CA file in PEM format (check OpenSSL docs, implies\r\n" + 
				"                      --ssl).\r\n" + 
				"  --ssl-capath=name   CA directory (check OpenSSL docs, implies --ssl).\r\n" + 
				"  --ssl-cert=name     X509 cert in PEM format (implies --ssl).\r\n" + 
				"  --ssl-cipher=name   SSL cipher to use (implies --ssl).\r\n" + 
				"  --ssl-key=name      X509 key in PEM format (implies --ssl).\r\n" + 
				"  --ssl-crl=name      Certificate revocation list (implies --ssl).\r\n" + 
				"  --ssl-crlpath=name  Certificate revocation list path (implies --ssl).\r\n" + 
				"  --ssl-verify-server-cert \r\n" + 
				"                      Verify server's \"Common Name\" in its cert against\r\n" + 
				"                      hostname used when connecting. This option is disabled by\r\n" + 
				"                      default.\r\n" + 
				"  --ssl-mode=name     SSL connection mode.\r\n" + 
				"  -t, --table         Output in table format.\r\n" + 
				"  --tee=name          Append everything into outfile. See interactive help (\\h)\r\n" + 
				"                      also. Does not work in batch mode. Disable with\r\n" + 
				"                      --disable-tee. This option is disabled by default.\r\n" + 
				"  -u, --user=name     User for login if not current user.\r\n" + 
				"  -U, --safe-updates  Only allow UPDATE and DELETE that uses keys.\r\n" + 
				"  -U, --i-am-a-dummy  Synonym for option --safe-updates, -U.\r\n" + 
				"  -v, --verbose       Write more. (-v -v -v gives the table output format).\r\n" + 
				"  -V, --version       Output version information and exit.\r\n" + 
				"  -w, --wait          Wait and retry if connection is down.\r\n" + 
				"  --connect-timeout=# Number of seconds before connection timeout.\r\n" + 
				"  --max-allowed-packet=# \r\n" + 
				"                      The maximum packet length to send to or receive from\r\n" + 
				"                      server.\r\n" + 
				"  --net-buffer-length=# \r\n" + 
				"                      The buffer size for TCP/IP and socket communication.\r\n" + 
				"  --select-limit=#    Automatic limit for SELECT when using --safe-updates.\r\n" + 
				"  --max-join-size=#   Automatic limit for rows in a join when using\r\n" + 
				"                      --safe-updates.\r\n" + 
				"  --secure-auth       Refuse client connecting to server if it uses old\r\n" + 
				"                      (pre-4.1.1) protocol.\r\n" + 
				"                      (Defaults to on; use --skip-secure-auth to disable.)\r\n" + 
				"  --server-arg=name   Send embedded server this as a parameter.\r\n" + 
				"  --show-warnings     Show warnings after every statement.\r\n" + 
				"  --plugin-dir=name   Directory for client-side plugins.\r\n" + 
				"  --default-auth=name Default authentication client-side plugin to use.\r\n" + 
				"  --histignore=name   A colon-separated list of patterns to keep statements\r\n" + 
				"                      from getting logged into mysql history.\r\n" + 
				"  --binary-mode       By default, ASCII '\\0' is disallowed and '\\r\\n' is\r\n" + 
				"                      translated to '\\n'. This switch turns off both features,\r\n" + 
				"                      and also turns off parsing of all clientcommands except\r\n" + 
				"                      \\C and DELIMITER, in non-interactive mode (for input\r\n" + 
				"                      piped to mysql or loaded using the 'source' command).\r\n" + 
				"                      This is necessary when processing output from mysqlbinlog\r\n" + 
				"                      that may contain blobs.\r\n" + 
				"  --connect-expired-password \r\n" + 
				"                      Notify the server that this client is prepared to handle\r\n" + 
				"                      expired password sandbox mode.\r\n" + 
				"\r\n" + 
				"Default options are read from the following files in the given order:\r\n" + 
				"/etc/my.cnf /etc/mysql/my.cnf /usr/etc/my.cnf ~/.my.cnf \r\n" + 
				"The following groups are read: mysql client\r\n" + 
				"The following options may be given as the first argument:\r\n" + 
				"--print-defaults        Print the program argument list and exit.\r\n" + 
				"--no-defaults           Don't read default options from any option file,\r\n" + 
				"                        except for login file.\r\n" + 
				"--defaults-file=#       Only read default options from the given file #.\r\n" + 
				"--defaults-extra-file=# Read this file after the global files are read.\r\n" + 
				"--defaults-group-suffix=#\r\n" + 
				"                        Also read groups with concat(group, suffix)\r\n" + 
				"--login-path=#          Read this path from the login file.\r\n" + 
				"\r\n" + 
				"Variables (--variable-name=value)\r\n" + 
				"and boolean options {FALSE|TRUE}  Value (after reading options)\r\n" + 
				"--------------------------------- ----------------------------------------\r\n" + 
				"auto-rehash                       TRUE\r\n" + 
				"auto-vertical-output              FALSE\r\n" + 
				"bind-address                      (No default value)\r\n" + 
				"binary-as-hex                     FALSE\r\n" + 
				"character-sets-dir                (No default value)\r\n" + 
				"column-type-info                  FALSE\r\n" + 
				"comments                          FALSE\r\n" + 
				"compress                          FALSE\r\n" + 
				"debug-check                       FALSE\r\n" + 
				"debug-info                        FALSE\r\n" + 
				"database                          (No default value)\r\n" + 
				"default-character-set             auto\r\n" + 
				"delimiter                         ;\r\n" + 
				"enable-cleartext-plugin           FALSE\r\n" + 
				"vertical                          FALSE\r\n" + 
				"force                             FALSE\r\n" + 
				"named-commands                    FALSE\r\n" + 
				"ignore-spaces                     FALSE\r\n" + 
				"init-command                      (No default value)\r\n" + 
				"local-infile                      FALSE\r\n" + 
				"no-beep                           FALSE\r\n" + 
				"host                              (No default value)\r\n" + 
				"html                              FALSE\r\n" + 
				"xml                               FALSE\r\n" + 
				"line-numbers                      TRUE\r\n" + 
				"unbuffered                        FALSE\r\n" + 
				"column-names                      TRUE\r\n" + 
				"sigint-ignore                     FALSE\r\n" + 
				"port                              0\r\n" + 
				"prompt                            mysql> \r\n" + 
				"quick                             FALSE\r\n" + 
				"raw                               FALSE\r\n" + 
				"reconnect                         TRUE\r\n" + 
				"socket                            (No default value)\r\n" + 
				"ssl                               FALSE\r\n" + 
				"ssl-ca                            (No default value)\r\n" + 
				"ssl-capath                        (No default value)\r\n" + 
				"ssl-cert                          (No default value)\r\n" + 
				"ssl-cipher                        (No default value)\r\n" + 
				"ssl-key                           (No default value)\r\n" + 
				"ssl-crl                           (No default value)\r\n" + 
				"ssl-crlpath                       (No default value)\r\n" + 
				"ssl-verify-server-cert            FALSE\r\n" + 
				"table                             FALSE\r\n" + 
				"user                              (No default value)\r\n" + 
				"safe-updates                      FALSE\r\n" + 
				"i-am-a-dummy                      FALSE\r\n" + 
				"connect-timeout                   0\r\n" + 
				"max-allowed-packet                16777216\r\n" + 
				"net-buffer-length                 16384\r\n" + 
				"select-limit                      1000\r\n" + 
				"max-join-size                     1000000\r\n" + 
				"secure-auth                       TRUE\r\n" + 
				"show-warnings                     FALSE\r\n" + 
				"plugin-dir                        (No default value)\r\n" + 
				"default-auth                      (No default value)\r\n" + 
				"histignore                        (No default value)\r\n" + 
				"binary-mode                       FALSE\r\n" + 
				"connect-expired-password          FALSE\r\n" + 
				"[root@localhost ~]# ";
		
		Matcher m = MysqlUtil.MYSQL_HELP_MY_CNF.matcher(s);
		assertTrue(m.matches());
		String s1 = m.group(1);
		assertThat(s1, equalTo("/etc/my.cnf /etc/mysql/my.cnf /usr/etc/my.cnf ~/.my.cnf "));
	}

	@Test
	public void tLogin() throws IOException, JSchException {
		Channel channel = sshSession.openChannel("shell");
		channel.connect();

		// @formatter:off
		Expect expect = new ExpectBuilder()
				.withOutput(channel.getOutputStream())
				.withInputs(channel.getInputStream(), channel.getExtInputStream())
//				.withEchoOutput(System.out)
//				.withEchoInput(System.err)
//				.withExceptionOnFailure()
				.build();
		try {
			String nextCmd = "mysql --help --verbose"; 
			expect.sendLine(nextCmd);
			String result = expect.expect(times(2, contains("]# "))).getBefore();
			Iterator<String> it = StringUtil.splitLines(result).iterator();
			String possibleCnfFiles = null;
			while (it.hasNext()) {
				String line = (String) it.next();
				if (line.indexOf("Default options are read from the following files in the given order:") != -1) {
					if (it.hasNext()) {
						possibleCnfFiles = it.next().trim();
					}
				}
			}
			nextCmd = String.format("ls %s", Stream.of(possibleCnfFiles.split("\\s+")).map(s -> s.trim()).collect(Collectors.joining(" ")));
			ExpectitUtil.comsumeInputs(expect, contains("]#"));
			expect.sendLine(nextCmd);
			result = expect.expect(contains("]#")).getBefore();
			String line = StringUtil.splitLines(result).stream().map(s -> s.trim()).filter(s -> s.indexOf(' ') == -1).findFirst().get();
			assertThat(line, equalTo("/etc/my.cnf"));
//			 ls /etc/my.cnf /etc/mysql/my.cnf /usr/etc/my.cnf ~/.my.cnf
//			 ls: cannot access /etc/mysql/my.cnf: No such file or directory
//			 ls: cannot access /usr/etc/my.cnf: No such file or directory
//			 ls: cannot access /root/.my.cnf: No such file or directory
//			 /etc/my.cnf
//			 [root@localhost ~
			
			
			System.out.println(result);
//			expect.send("N");
//			expect.expect(regexp(": $"));
		} finally {
			expect.close();
			channel.disconnect();
		}

	}

}
