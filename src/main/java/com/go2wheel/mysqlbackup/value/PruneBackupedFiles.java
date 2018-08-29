package com.go2wheel.mysqlbackup.value;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.util.FileUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PruneBackupedFiles {
	
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
	public Map<Instant, List<PathAndCt>> prune(String prunePattern) {
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

	protected Map<Instant, List<PathAndCt>> prune(List<PathAndCt> backups, int secondly, int minutely, int hourly,
			int daily, int weekly, int monthly, int yearly) {
		List<PathAndCt> listOfAc = backups;
		listOfAc = byMinutes(listOfAc, secondly).values().stream().flatMap(la -> la.stream())
				.collect(Collectors.toList());
		listOfAc = byHours(listOfAc, minutely).values().stream().flatMap(la -> la.stream())
				.collect(Collectors.toList());
		listOfAc = byDays(listOfAc, hourly).values().stream().flatMap(la -> la.stream()).collect(Collectors.toList());
//		listOfAc = byWeeks(listOfAc, daily).values().stream().flatMap(la -> la.stream()).collect(Collectors.toList());
		listOfAc = byMonthes(listOfAc, weekly).values().stream().flatMap(la -> la.stream())
				.collect(Collectors.toList());

		Map<Instant, List<PathAndCt>> yearsmap = byYears(listOfAc, monthly);
		if (yearly > 0) {
			yearsmap = process(yearsmap, yearly);
		}
		return yearsmap;
	}

	public List<PathAndCt> init() {
		List<PathAndCt> lst = Lists.newArrayList();
		String ofn = this.origin.getFileName().toString();
		Pattern ptn = Pattern.compile(ofn + "\\.\\d+$");
		try {
			lst = Files.list(origin.toAbsolutePath().getParent())
					.filter(p -> ptn.matcher(p.getFileName().toString()).matches()).map(p -> new PathAndCt(p))
					.filter(p -> p.getInstant() != null && p.getPath() != null).collect(toList());
		} catch (IOException e) {
		}
		return lst;
	}

	private Map<Instant, List<PathAndCt>> process(Map<Instant, List<PathAndCt>> rs, int keepCount) {
		if (keepCount == 0) {
			return rs;
		}
		Map<Instant, List<PathAndCt>> nrs = Maps.newTreeMap();

		rs.entrySet().stream().limit(1).forEach(en -> {
			nrs.put(en.getKey(), en.getValue());
		});

		rs.entrySet().stream().skip(1).forEach(en -> {
			Instant k = en.getKey();

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

	public Map<Instant, List<PathAndCt>> byMinutes(List<PathAndCt> pcts, int keepCount) {
		Map<Instant, List<PathAndCt>> rs = pcts.stream()
				.collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.MINUTES), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	public Map<Instant, List<PathAndCt>> byHours(List<PathAndCt> pcts, int keepCount) {
		Map<Instant, List<PathAndCt>> rs = pcts.stream()
				.collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.HOURS), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	public Map<Instant, List<PathAndCt>> byDays(List<PathAndCt> pcts, int keepCount) {
		Map<Instant, List<PathAndCt>> rs = pcts.stream()
				.collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.DAYS), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

//	public Map<Instant, List<PathAndCt>> byWeeks(List<PathAndCt> pcts, int keepCount) {
//		Map<Instant, List<PathAndCt>> rs = pcts.stream()
//				.collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.WEEKS), TreeMap::new, toList()));
//		return process(rs, keepCount);
//	}

	public Map<Instant, List<PathAndCt>> byMonthes(List<PathAndCt> pcts, int keepCount) {
		Map<Instant, List<PathAndCt>> rs = pcts.stream()
				.collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.MONTHS), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	public Map<Instant, List<PathAndCt>> byYears(List<PathAndCt> pcts, int keepCount) {
		Map<Instant, List<PathAndCt>> rs = pcts.stream()
				.collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.YEARS), TreeMap::new, toList()));
		return process(rs, keepCount);
	}

	protected static class PathAndCt implements Comparable<PathAndCt> {
		private Path path;
		private Instant instant;

		public PathAndCt() {
		}

		public PathAndCt(Path path) {
			if (Files.exists(path)) {
				this.path = path;
				try {
					BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
					instant = attr.creationTime().toInstant();
				} catch (IOException e) {
				}
			}
		}

		public Path getPath() {
			return path;
		}

		public void setPath(Path path) {
			this.path = path;
		}

		public Instant getInstant() {
			return instant;
		}

		public void setInstant(Instant instant) {
			this.instant = instant;
		}

		@Override
		public int compareTo(PathAndCt o) {
			return this.getInstant().compareTo(o.getInstant());
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
