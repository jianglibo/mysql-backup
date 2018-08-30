package com.go2wheel.mysqlbackup.value;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.util.FileUtil;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 将备份文件按不同的粒度分组，同时保留排位最新的分组不受干扰。比如按分钟分组，如果要保留secondly是5个，那么所有的分钟分组中都保留5个secondly。在下一轮分组中，比如以小时分组，此时对最新一组的5个secondly不做处理，但是对其它分组的5个secondly计入minutely分组，
 * 如果minutely是2个，那么5个中的3个将被minutely剔除。以此类推。
 * 如果小颗粒的保留个数大于大颗粒的单位，那么大颗粒的值将影响小颗粒的设定。比如，设定secondly的值是70，那么minutely分组后，其中10个将归入另一个minutely单元。
 * 
 * @author jianglibo@gmail.com
 *
 */
public class PruneBackupedFiles {

	public static final String DF_STRING = "yyyyMMddHHmmssS";
	
	public static DateTimeFormatter TILL_MINUTE = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
	public static DateTimeFormatter TILL_HOUR = DateTimeFormatter.ofPattern("yyyyMMddHH");
	public static DateTimeFormatter TILL_DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
	public static DateTimeFormatter TILL_WEEK = DateTimeFormatter.ofPattern("yyyyMMw");
	public static DateTimeFormatter TILL_MONTH = DateTimeFormatter.ofPattern("yyyyMM");
	public static DateTimeFormatter TILL_YEAR = DateTimeFormatter.ofPattern("yyyy");

	private Path origin;
	private final boolean originExists;

	public PruneBackupedFiles(Path origin) {
		this.origin = origin;
		originExists = Files.exists(origin);
	}

	private int getKeepCount(Matcher m, int group) {
		String ds = m.group(group);
		return Integer.valueOf(ds);
	}

	// secondly, minutely, hourly, daily, weekly, monthly, yearly.
	public Map<String, List<PathAndCt>> prune(String prunePattern) {
		Pattern ptn = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*$");
		Matcher m = ptn.matcher(prunePattern);
		if (m.matches()) {
			int secondly = getKeepCount(m, 1);
			int minutely = getKeepCount(m, 2);
			int hourly = getKeepCount(m, 3);
			int daily = getKeepCount(m, 4);
			int weekly = getKeepCount(m, 5);
			int monthly = getKeepCount(m, 6);
			int yearly = getKeepCount(m, 7);
			return prune(init(), secondly, minutely, hourly, daily, weekly, monthly, yearly);
		}
		return null;
	}

	protected Map<String, List<PathAndCt>> prune(List<PathAndCt> backups, int secondly, int minutely, int hourly,
			int daily, int weekly, int monthly, int yearly) {
		List<PathAndCt> listOfAc = backups;
		Collections.sort(listOfAc);
		if (secondly > 0)
			listOfAc = byMinutes(listOfAc, secondly).values().stream().flatMap(la -> la.stream())
					.collect(Collectors.toList());
		if (minutely > 0)
			listOfAc = byHours(listOfAc, minutely).values().stream().flatMap(la -> la.stream())
					.collect(Collectors.toList());
		if (hourly > 0)
			listOfAc = byDays(listOfAc, hourly).values().stream().flatMap(la -> la.stream()).collect(Collectors.toList());
		if (daily > 0) 
			listOfAc = byWeeks(listOfAc, daily).values().stream().flatMap(la -> la.stream()).collect(Collectors.toList());
		if (weekly > 0)
			listOfAc = byMonthes(listOfAc, weekly).values().stream().flatMap(la -> la.stream())
					.collect(Collectors.toList());
		
		if (monthly > 0)
			listOfAc = byYears(listOfAc, monthly).values().stream().flatMap(la -> la.stream())
					.collect(Collectors.toList());
			return listOfAc.stream().collect(Collectors.groupingBy(ptc -> ptc.getCt(), TreeMap::new, toList()));
	}

	public List<PathAndCt> init() {
		List<PathAndCt> lst = Lists.newArrayList();
		String ofn = this.origin.getFileName().toString();
		Pattern ptn = Pattern.compile(ofn + "\\.\\d+$");
		try {
			lst = Files.list(origin.toAbsolutePath().getParent())
					.filter(p -> ptn.matcher(p.getFileName().toString()).matches()).map(p -> new PathAndCt(p))
					.filter(p -> p.getCt() != null && p.getPath() != null).collect(toList());
		} catch (IOException e) {
		}
		return lst;
	}

	private Map<String, List<PathAndCt>> process(Map<String, List<PathAndCt>> rs, int keepCount) {
		if (keepCount == 0) {
			return rs;
		}
		Map<String, List<PathAndCt>> nrs = Maps.newTreeMap();

		rs.entrySet().stream().skip(rs.size() - 1).forEach(en -> {
			nrs.put(en.getKey(), en.getValue());
		});

		rs.entrySet().stream().limit(rs.size() - 1).forEach(en -> {
			String k = en.getKey();

			List<PathAndCt> lpc = en.getValue();
			int dele = lpc.size() - keepCount;

			if (dele > 0) {
				List<PathAndCt> dellist = lpc.subList(0, dele);
				dellist.stream().filter(p -> p.getPath() != null).filter(p -> Files.exists(p.getPath())).forEach(p -> {
					Path pp = p.getPath();
					if (Files.isDirectory(pp)) {
						try {
							FileUtil.deleteFolder(pp, false);
						} catch (IOException e) {
						}
					} else {
						try {
							Files.delete(pp);
						} catch (IOException e) {
						}
					}
				});
				lpc = lpc.subList(dele, lpc.size());
			}
			nrs.put(k, lpc);
		});
		return nrs;
	}

	public Map<String, List<PathAndCt>> byMinutes(List<PathAndCt> pcts, int keepCount) {
		Map<String, List<PathAndCt>> rs = pcts.stream().map(pct -> pct.refreshCt(TILL_MINUTE))
				.collect(groupingBy(pct -> pct.getCt(), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	public Map<String, List<PathAndCt>> byHours(List<PathAndCt> pcts, int keepCount) {
		Map<String, List<PathAndCt>> rs = pcts.stream().map(pct -> pct.refreshCt(TILL_HOUR))
				.collect(groupingBy(pct -> pct.getCt(), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	public Map<String, List<PathAndCt>> byDays(List<PathAndCt> pcts, int keepCount) {
		Map<String, List<PathAndCt>> rs = pcts.stream().map(pct -> pct.refreshCt(TILL_DAY))
				.collect(groupingBy(pct -> pct.getCt(), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	public Map<String, List<PathAndCt>> byWeeks(List<PathAndCt> pcts, int keepCount) {
		Map<String, List<PathAndCt>> rs = pcts.stream().map(pct -> pct.refreshCt(TILL_WEEK))
				.collect(groupingBy(pct -> pct.getCt(), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	public Map<String, List<PathAndCt>> byMonthes(List<PathAndCt> pcts, int keepCount) {
		Map<String, List<PathAndCt>> rs = pcts.stream().map(pct -> pct.refreshCt(TILL_MONTH))
				.collect(groupingBy(pct -> pct.getCt(), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	public Map<String, List<PathAndCt>> byYears(List<PathAndCt> pcts, int keepCount) {
		Map<String, List<PathAndCt>> rs = pcts.stream().map(pct -> pct.refreshCt(TILL_YEAR))
				.collect(groupingBy(pct -> pct.getCt(), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	protected static class PathAndCt implements Comparable<PathAndCt> {
		private Path path;
		private String ct;
		private LocalDateTime ldt;

		public PathAndCt() {
		}

		public PathAndCt(Path path) {
			if (Files.exists(path)) {
				this.path = path;
				try {
					BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
					ldt = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
				} catch (IOException e) {
				}
			}
		}

		public PathAndCt refreshCt(DateTimeFormatter dtf) {
			setCt(getLdt().format(dtf));
			return this;
		}

		public Path getPath() {
			return path;
		}

		public void setPath(Path path) {
			this.path = path;
		}

		public String getCt() {
			return ct;
		}

		public void setCt(String ct) {
			this.ct = ct;
		}

		public LocalDateTime getLdt() {
			return ldt;
		}

		public void setLdt(LocalDateTime ldt) {
			this.ldt = ldt;
		}

		@Override
		public int compareTo(PathAndCt o) {
			return this.getLdt().compareTo(o.getLdt());
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("path", getPath() != null ? getPath().toAbsolutePath().toString() : "null")
					.add("ct", getCt())
					.add("ldt", getLdt().toString())
					.toString();
		}
	}

	public boolean isOriginExists() {
		return originExists;
	}

	public Path getOrigin() {
		return origin;
	}

	public void setOrigin(Path origin) {
		this.origin = origin;
	}

}
