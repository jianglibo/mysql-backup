package com.go2wheel.mysqlbackup.value;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.go2wheel.mysqlbackup.model.UpTime;

public class UptimeAllString {
	
//	http://blog.scoutapp.com/articles/2009/07/31/understanding-load-averages
//	 23:44:09 up  3:02,  1 user,  load average: 0.00, 0.01, 0.05
//	2018-05-19 20:41:51
//	up 3 hours, 6 minutes
//	 23:48:46 up  3:06,  1 user,  load average: 0.00, 0.01, 0.05
//	uptime -s;uptime -p;uptime
	
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
