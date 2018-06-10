package com.go2wheel.mysqlbackup.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.model.UpTime;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;

@Service
public class ServerStateService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UpTimeDbService upTimeDbService;
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private ServerStateDbService serverStateDbService;
	
	public UptimeAllString getUpTime(Session session) {
		String command = "uptime -s;uptime -p;uptime";
		try {
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, command);
			return UptimeAllString.build(rcr.getAllTrimedNotEmptyLines());
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return null;
		}
	}
	

//	public void createLinuxUptime(Server sv, Session session) {
//		UptimeAllString uta = getUpTime(session);
//		UpTime ut = uta.toUpTime();
//		if (sv.getCoreNumber() == 0) {
//			int cn = SSHcommonUtil.coreNumber(session);
//			sv.setCoreNumber(cn);
//			serverDbService.save(sv);
//		}
//		if (sv != null) {
//			ut.setServerId(sv.getId());
//			upTimeDbService.save(ut);
//		}
//	}
	
	
	public ServerState createLinuxServerState(Server server, Session session) throws RunRemoteCommandException {
		ServerState ss = new ServerState();
		
		UptimeAllString uta = getUpTime(session);
		int load = (int) (Float.parseFloat(uta.getLoadFifteen()) * 100);
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
	
	public ServerState createWinServerState(Server server, Session session) throws IOException {
		ServerState ss = new ServerState();
		String command = "Get-CimInstance -ClassName win32_operatingsystem | Format-List -Property *"; // FreePhysicalMemory, TotalVisibleMemorySize
		RemoteCommandResult rcr = PSUtil.runPsCommand(command);
		Map<String, String> mss = PSUtil.parseFormatList(rcr.getStdOutList()).get(0);
		String lbt = mss.get("LastBootUpTime");
		ss.setMemFree(StringUtil.parseLong(mss.get("FreePhysicalMemory")));
		ss.setMemUsed(StringUtil.parseLong(mss.get("TotalVisibleMemorySize")) - ss.getMemFree());
		
		command = "Get-WmiObject win32_processor | Format-List -Property *";
		rcr = PSUtil.runPsCommand(command);
		mss = PSUtil.parseFormatList(rcr.getStdOutList()).get(0); // LoadPercentage, NumberOfCores
		
		ss.setAverageLoad(StringUtil.parseInt(mss.get("LoadPercentage")));
		ss.setServerId(server.getId());
		return serverStateDbService.save(ss);
	}

	protected static class UptimeAllString {
//		http://blog.scoutapp.com/articles/2009/07/31/understanding-load-averages
//		 23:44:09 up  3:02,  1 user,  load average: 0.00, 0.01, 0.05
//		2018-05-19 20:41:51
//		up 3 hours, 6 minutes
//		 23:48:46 up  3:06,  1 user,  load average: 0.00, 0.01, 0.05
//		uptime -s;uptime -p;uptime
		private  String since = "";
		private  String uptime = "";
		private  String loadOne = "";
		private  String loadFive = "";
		private  String loadFifteen = "";
		
		private UptimeAllString() {}
		
		public static UptimeAllString build(List<String> out) {
			UptimeAllString ua = null;
			try {
				ua = new UptimeAllString();
				ua.since = out.get(0).trim();
				ua.uptime = out.get(1).trim().replaceFirst("up\\s*", "");
				String pstr = "average:";
				int i = out.get(2).indexOf(pstr) + pstr.length();
				String[] ss = out.get(2).substring(i).trim().split(",");
				if (ss.length != 3) {
					ss = out.get(2).substring(i).trim().split("\\s+");
				}
				ua.loadOne = ss[0].trim();
				ua.loadFive = ss[1].trim();
				ua.loadFifteen = ss[2].trim();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ua;
		}
		
		public UpTime toUpTime() {
			UpTime ut = new UpTime();
			ut.setCreatedAt(new Date());
			ut.setLoadFifteen((int) (Float.parseFloat(loadFifteen) * 100));
			ut.setLoadFive((int) (Float.parseFloat(loadFive) * 100));
			ut.setLoadOne((int) (Float.parseFloat(loadOne) * 100));
			
			Scanner scanner = new Scanner(uptime);
			int tm = 0;
			
			while(scanner.hasNextInt()) {
				int i = scanner.nextInt();
				int unit = scanUnit(scanner);
				tm += i * unit;
			}
			ut.setUptimeMinutes(tm);
			return ut;
		}
		
		public int scanUnit(Scanner scanner) {
			int uniti = 0;
			while(!scanner.hasNextInt()) { // remove all none fields till met integer.
				if (!scanner.hasNext())return uniti;
				String unit = scanner.next();
				if (unit.contains("minute")) {
					uniti =  1;
				} else if (unit.contains("hour")) {
					uniti =  60;
				} else if (unit.contains("day")) {
					uniti =  1440;
				}
			}
			return uniti;
		}

		public String getSince() {
			return since;
		}

		public String getUptime() {
			return uptime;
		}

		public String getLoadOne() {
			return loadOne;
		}
		public String getLoadFive() {
			return loadFive;
		}

		public String getLoadFifteen() {
			return loadFifteen;
		}
	}

}
