package com.go2wheel.mysqlbackup.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;

@Service
public class ServerStateService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	protected Pattern uptimePtn = Pattern.compile(".*average:.*\\s+(.*)$");
	
	@Autowired
	private ServerStateDbService serverStateDbService;
	
//	23:44:09 up  3:02,  1 user,  load average: 0.00, 0.01, 0.05
	private String getUpTime(Session session) {
		String command = "uptime";
		try {
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, command);
			Optional<String> lineOp = rcr.getAllTrimedNotEmptyLines().stream().filter(l -> l.contains("average")).findAny();
			if (!lineOp.isPresent()) {
				throw new UnExpectedContentException(rcr.getAllTrimedNotEmptyLines().stream().collect(Collectors.joining("\n")));
			}
			
			String line = lineOp.get();
			Matcher m = uptimePtn.matcher(line);
			
			if (!m.matches()) {
				throw new UnExpectedContentException(rcr.getAllTrimedNotEmptyLines().stream().collect(Collectors.joining("\n")));
			}
			return m.group(1);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return null;
		}
	}
	
	public int getCoreNumber(Server server, Session session) {
		if (session == null) {
			String command = "Get-WmiObject win32_processor | Format-List -Property *";
			ProcessExecResult pcr = PSUtil.runPsCommand(command);
			Map<String, String> mss = PSUtil.parseFormatList(pcr.getStdOutFilterEmpty()).get(0); // LoadPercentage, NumberOfCores
			return StringUtil.parseInt(mss.get("NumberOfCores"));
		}
		return SSHcommonUtil.coreNumber(session);
	}
	
	public ServerState createServerState(Server server, Session session) {
		if ("localhost".equals(server.getHost())) {
			return createWinServerState(server, session);
		} else {
			return createLinuxServerState(server, session);
		}
	}
	
	public ServerState createLinuxServerState(Server server, Session session) {
		ServerState ss = new ServerState();
		String loadstr = getUpTime(session);
		int load = (int) (Float.parseFloat(loadstr) * 100);
		ss.setAverageLoad(load);
		ss.setServerId(server.getId());
		
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "free -b");
//        total        used        free      shared  buff/cache   available
//        Mem:      511934464   189591552   155312128     4567040   167030784   280465408
//        Swap:    1073737728           0  1073737728
		List<String> lines = rcr.getAllTrimedNotEmptyLines();
		String line = lines.stream().filter(l -> l.contains("Mem")).findAny().get();
		String[] fields = line.split("\\s+");
		ss.setMemFree(StringUtil.parseLong(fields[2]));
		ss.setMemUsed(StringUtil.parseLong(fields[1]));
		return serverStateDbService.save(ss);
	}
	
	public ServerState createWinServerState(Server server, Session session) {
		ServerState ss = new ServerState();
		String command = "Get-CimInstance -ClassName win32_operatingsystem | Format-List -Property *"; // FreePhysicalMemory, TotalVisibleMemorySize
		ProcessExecResult pcr = PSUtil.runPsCommand(command);
		Map<String, String> mss = PSUtil.parseFormatList(pcr.getStdOutFilterEmpty()).get(0);
		String lbt = mss.get("LastBootUpTime");
		ss.setMemFree(StringUtil.parseLong(mss.get("FreePhysicalMemory")) * 1024);
		ss.setMemUsed(StringUtil.parseLong(mss.get("TotalVisibleMemorySize"))  * 1024 - ss.getMemFree());
		
		command = "Get-WmiObject win32_processor | Format-List -Property *";
		pcr = PSUtil.runPsCommand(command);
		mss = PSUtil.parseFormatList(pcr.getStdOutFilterEmpty()).get(0); // LoadPercentage, NumberOfCores
		
		ss.setAverageLoad(StringUtil.parseInt(mss.get("LoadPercentage")));
		ss.setServerId(server.getId());
		return serverStateDbService.save(ss);
	}

}
