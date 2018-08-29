package com.go2wheel.mysqlbackup.value;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public class PruneBackupedFiles {
	
	private Path origin;
	private final boolean originExists;
	private List<Path> backups;
	private int nextInt;
	
	public PruneBackupedFiles(Path origin) {
		this.origin = origin;
		originExists = Files.exists(origin);
	}
	
	// secondly, minutely, hourly, daily, weekly, monthly, yearly.
	public void prune(String prunePattern) {
		Pattern ptn = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*$");
		Matcher m = ptn.matcher(prunePattern);
		if (m.matches()) {
			for(int idx = 1; idx < 8; idx++) {
				String ds = m.group(idx);
				int keepCount = Integer.valueOf(ds);
				if (keepCount > 0) {
					
				}
			}
		}
	}
	
	
	public Map<Instant, List<PathAndCt>> byMinutes(List<PathAndCt> pcts, int keepCount) {
		Map<Instant, List<PathAndCt>> rs =	pcts.stream().collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.MINUTES), TreeMap::new, toList()));
		if (rs.size() > 1) {
			Iterator<List<PathAndCt>> it = rs.values().iterator();
			it.next();
			while(it.hasNext()) {
				List<PathAndCt> lpc = it.next();
				int dele = lpc.size() - keepCount;
				for(int i =0; i< dele; i++) {
					lpc.remove(0);
				}
			}
		}
		return rs;
	}
	
	public Map<Instant, List<PathAndCt>> byHours(List<PathAndCt> pcts) {
		return pcts.stream().collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.HOURS), TreeMap::new, toList()));
	}
	
	public Map<Instant, List<PathAndCt>> byDays(List<PathAndCt> pcts) {
		return pcts.stream().collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.DAYS), TreeMap::new, toList()));
	}
	
	public Map<Instant, List<PathAndCt>> byWeeks(List<PathAndCt> pcts) {
		return pcts.stream().collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.WEEKS), TreeMap::new, toList()));
	}
	
	public Map<Instant, List<PathAndCt>> byMonthes(List<PathAndCt> pcts) {
		return pcts.stream().collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.MONTHS), TreeMap::new, toList()));
	}
	
	public Map<Instant, List<PathAndCt>> byYears(List<PathAndCt> pcts) {
		return pcts.stream().collect(groupingBy(pct -> pct.getInstant().truncatedTo(ChronoUnit.YEARS), TreeMap::new, toList()));
	}
	
	protected static class PathAndCt implements Comparable<PathAndCt>{
		private Path path;
		private Instant instant;
		
		public PathAndCt(){}
		
		public PathAndCt(Path path) {
			this.path = path;
			try {
				BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				instant = attr.creationTime().toInstant();
			} catch (IOException e) {
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
	
	public PruneBackupedFiles init() {
		this.backups = Lists.newArrayList();
		String ofn = this.origin.getFileName().toString();
		Pattern ptn = Pattern.compile(ofn + "\\.\\d+$");
		try {
			this.backups = Files.list(origin.toAbsolutePath().getParent()).filter(p -> ptn.matcher(p.getFileName().toString()).matches()).collect(toList());
			Collections.sort(this.backups, new Comparator<Path>() {

				@Override
				public int compare(Path o1, Path o2) {
					try {
						BasicFileAttributes attr1 = Files.readAttributes(o1, BasicFileAttributes.class);
						BasicFileAttributes attr2 = Files.readAttributes(o2, BasicFileAttributes.class);
						return attr1.creationTime().toInstant().compareTo(attr2.creationTime().toInstant());
					} catch (IOException e) {
					}
					return 0;
				}
				
			});
		} catch (IOException e) {
		}
		return this;
	}
	public boolean isOriginExists() {
		return originExists;
	}

	public List<Path> getBackups() {
		return backups;
	}
	public void setBackups(List<Path> backups) {
		this.backups = backups;
	}
	public Path getOrigin() {
		return origin;
	}
	public void setOrigin(Path origin) {
		this.origin = origin;
	}
	public int getNextInt() {
		return nextInt;
	}
	public void setNextInt(int nextInt) {
		this.nextInt = nextInt;
	}

}
