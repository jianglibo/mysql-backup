package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.Collectors.*;

import java.util.stream.Collectors;

import org.junit.Test;

import com.go2wheel.mysqlbackup.value.PruneBackupedFiles.PathAndCt;
import com.google.common.collect.Lists;

public class TestPruneBackups {
	
	private List<PathAndCt> createSecondly(LocalDateTime ldt, int num) { // wouldn't change ldt in body of function.
		List<PathAndCt> pcts = Lists.newArrayList();
		for (int i = 0; i < num; i++) { // 2018-12-27 23:59:58 --> 2018-12-27 23:59:54 Total is 5 items.
			PathAndCt pct = new PathAndCt();
			pct.setLdt(ldt.minusSeconds(i));
			pcts.add(pct);
		}
		return pcts;
	}
	
	private List<PathAndCt> createMinutely(LocalDateTime ldt, int num) { // wouldn't change ldt in body of function.
		List<PathAndCt> pcts = Lists.newArrayList();
		for(int minuteOfHour = 0; minuteOfHour < num; minuteOfHour++) {
			LocalDateTime nldt = ldt.minusMinutes(minuteOfHour);
			pcts.addAll(createSecondly(nldt, num));
		}
		return pcts;
	}
	
	private List<PathAndCt> createHourly(LocalDateTime ldt, int num) {
		List<PathAndCt> pcts = Lists.newArrayList();
		for(int hourOfDay = 0; hourOfDay < num; hourOfDay++) {
			LocalDateTime nldt = ldt.minusHours(hourOfDay);
			pcts.addAll(createMinutely(nldt, num));
		}
		return pcts;
	}
	
	private List<PathAndCt> createDaily(LocalDateTime ldt, int num) {
		List<PathAndCt> pcts = Lists.newArrayList();
		for(int dayOfMonth = 0; dayOfMonth < num; dayOfMonth++) {
			LocalDateTime nldt = ldt.minusDays(dayOfMonth);
			pcts.addAll(createHourly(nldt, num));
		}
		return pcts;
	}
	
	private List<PathAndCt> createWeekly(LocalDateTime ldt, int num) {
		List<PathAndCt> pcts = Lists.newArrayList();
		for(int weekOfYear = 0; weekOfYear < num; weekOfYear++) {
			LocalDateTime nldt = ldt.minusWeeks(weekOfYear);
			pcts.addAll(createDaily(nldt, num));
		}
		return pcts;
	}
	
	private List<PathAndCt> createMonthly(LocalDateTime ldt, int num) {
		List<PathAndCt> pcts = Lists.newArrayList();
		for(int monthOfYear = 0; monthOfYear < num; monthOfYear++) {
			LocalDateTime nldt = ldt.minusMonths(monthOfYear);
			pcts.addAll(createWeekly(nldt, num));
		}
		return pcts;
	}
	
	private List<PathAndCt> createYearly(LocalDateTime ldt, int num) {
		List<PathAndCt> pcts = Lists.newArrayList();
		for(int yearDelta = 0; yearDelta < num; yearDelta++) {
			LocalDateTime nldt = ldt.minusYears(yearDelta);
			pcts.addAll(createMonthly(nldt, num));
		}
		return pcts;
	}


	private List<PathAndCt> fixturesInMinutes(int num) {
		List<PathAndCt> pcts = Lists.newArrayList();
		LocalDateTime ldt = LocalDateTime.of(2018, 11, 27, 23, 59, 59);
		pcts.addAll(createYearly(ldt, num)); // month 11,10,9,8
		Collections.sort(pcts);
		return pcts;
	}
	

	@Test
	public void tEachRange() {
		PruneBackupedFiles pbf = new PruneBackupedFiles(Paths.get(""));
		int total = (int) Math.pow(3, 7);
		List<PathAndCt> lists = fixturesInMinutes(3);
		assertThat(lists.size(), equalTo(total));

		Map<String, List<PathAndCt>> byMonthes = pbf.byMonthes(lists, 2000);
		
		assertThat(byMonthes.size(), equalTo(9));
		assertThat(byMonthes.keySet().stream().skip(byMonthes.size() - 2).collect(Collectors.toList()), contains("201810", "201811")); // each month group contains 256 items.
		assertThat(byMonthes.values().stream().flatMap(pp -> pp.stream()).count(), equalTo((long)total));
		
		Map<String, List<PathAndCt>> byMonthes1 = pbf.byMonthes(lists, 20);
		List<PathAndCt> latestMonth = byMonthes1.get("201811");
		assertThat(latestMonth.size(), equalTo(243));
		
		byMonthes1.values().stream().limit(byMonthes1.size() - 1).forEach(ll -> {
			assertThat(ll.size(), equalTo(20));
		});
		
		
		assertThat(byMonthes1.values().stream().flatMap(pp -> pp.stream()).count(), equalTo(243L + 20 * 8)); // The other months except last one have 20 items each.
		
		Map<String, List<PathAndCt>> byDays = pbf.byDays(lists, 2000);
		
		assertThat(byDays.size(), equalTo(81));
		assertThat(byDays.keySet().stream().skip(byDays.size() - 2).collect(toList()), contains("20181126", "20181127")); // each month group contains 256 items.
		assertThat(byDays.values().stream().flatMap(pp -> pp.stream()).count(), equalTo((long)total));
		
		Map<String, List<PathAndCt>> byDays1 = pbf.byDays(lists, 20);
		
		byDays1.values().stream().limit(byDays.size() - 1).forEach(v -> {
			assertThat(v.size(), equalTo(20));
		});
		
		byDays1.values().stream().skip(byDays.size() - 1).forEach(v -> {
			assertThat(v.size(), equalTo(27));
		});
		
		Map<String, List<PathAndCt>> byminutes = pbf.byMinutes(lists, 40);
		assertThat(byminutes.size(), equalTo(729));
		assertThat(byminutes.values().stream().flatMap(pp -> pp.stream()).count(), equalTo((long)total));
		
		Map<String, List<PathAndCt>> byminutes1 = pbf.byMinutes(lists, 1);
		assertThat(byminutes1.values().stream().flatMap(pp -> pp.stream()).count(), equalTo(728L + 3));
		
		Map<String, List<PathAndCt>> byWeeks = pbf.byWeeks(lists, 400);
		assertThat(byWeeks.size(), equalTo(29));
		assertThat(byWeeks.values().stream().flatMap(pp -> pp.stream()).count(), equalTo((long)total));
		
		Map<String, List<PathAndCt>> byWeeks1 = pbf.byWeeks(lists, 1);
		
		byWeeks1.values().stream().limit(byWeeks1.size() - 1).forEach(v -> {
			assertThat(v.size(), equalTo(1));
		});
		
		byWeeks1.values().stream().skip(byWeeks1.size() - 1).forEach(v -> {
			assertThat(v.size(), equalTo(81));
		});
		assertThat(byWeeks1.values().stream().flatMap(pp -> pp.stream()).count(), equalTo(81L + 28));
	}
	
	@Test
	public void tReal1() {
		PruneBackupedFiles pbf = new PruneBackupedFiles(Paths.get(""));
		List<PathAndCt> lists = fixturesInMinutes(3);
		
		// keep latest 2 secondly, 2 minutely, 2 hourly, 2 daily, 2 weekly, 2 monthly.
		Map<String, List<PathAndCt>> m = pbf.prune(lists, 2, 2, 2, 2, 2, 2, 0);
		assertThat(m.size(), equalTo(3));
		Map<String, List<PathAndCt>> msec = m.values()
				.stream()
				.flatMap(pp -> pp.stream())
				.map(pc -> pc.refreshCt(PruneBackupedFiles.TILL_MINUTE))
				.collect(groupingBy(
						pc -> pc.getCt(),
						TreeMap::new,
						toList()));
//		msec.keySet().stream().forEach(System.out::println);
		assertThat(msec.size(), equalTo(13));
		assertThat(msec.values().stream().mapToInt(l -> l.size()).sum(), equalTo(27)); // the last group has 4 items.
		
//		201611272359 2 in another year.
//		201711272359 2 in another year.
//		201809272359 2 in another month.
//		201810272359 2 in another month.
//		201811132359 2 in another week.
//		201811202359 2 in another week.
//		201811252359 2 in day 25th.
//		201811262359 2 in day 26th.
//		201811272159 2 in hour 21th.
//		201811272259 2 in hour 22th.
//		201811272357 2 in minute 57th.
//		201811272358 2 in minute 58th.
//		201811272359 3 in minute 59th.
	}
	
	@Test
	public void tReal2() {
		PruneBackupedFiles pbf = new PruneBackupedFiles(Paths.get(""));
		List<PathAndCt> lists = fixturesInMinutes(3);
		
		// 2 hourly, 2 daily, 2 weekly, 2 monthly.
		Map<String, List<PathAndCt>> m = pbf.prune(lists, 0, 0, 2, 2, 2, 2, 2);
		assertThat(m.size(), equalTo(3));
		Map<String, List<PathAndCt>> msec = m.values()
				.stream()
				.flatMap(pp -> pp.stream())
				.map(pc -> pc.refreshCt(PruneBackupedFiles.TILL_HOUR))
				.collect(groupingBy(
						pc -> pc.getCt(),
						TreeMap::new,
						toList()));
		msec.keySet().stream().forEach(System.out::println);
		assertThat(msec.size(), equalTo(11));
		assertThat(msec.values().stream().mapToInt(l -> l.size()).sum(), equalTo(43)); // the last group has 4 items.
		
//		2016112723
//		2017112723
//		2018092723
//		2018102723
//		2018111323
//		2018112023
//		2018112523
//		2018112623
//		2018112721
//		2018112722
//		2018112723 
	}

	
	@Test
	public void testTruncated() {
		Instant is = Instant.ofEpochMilli(1545926399000L);
		Instant is1 = Instant.ofEpochMilli(1545926399000L + 55);
		
		LocalDateTime timePoint1 = LocalDateTime.ofInstant(is, ZoneId.systemDefault());
		LocalDateTime timePoint2 = LocalDateTime.ofInstant(is1, ZoneId.systemDefault());
		
		assertFalse(timePoint1.equals(timePoint2));
		
		LocalDateTime localDateTimeTruncated1 = timePoint1.truncatedTo(ChronoUnit.SECONDS);
		LocalDateTime localDateTimeTruncated2 = timePoint2.truncatedTo(ChronoUnit.SECONDS);
		assertTrue(localDateTimeTruncated1.equals(localDateTimeTruncated2));
	}
	
	@Test(expected=UnsupportedTemporalTypeException.class)
	public void getFields() {
		LocalDateTime timePoint1 = LocalDateTime.now();
		timePoint1.truncatedTo(ChronoUnit.MONTHS);
	}
	
	
	@Test
	public void tInstant() {
		// 1545926399000, 2018-12-27T15:59:59Z
		Instant is = Instant.ofEpochMilli(1545926399000L);
		
		LocalDateTime timePoint1 = LocalDateTime.of(2018, 11, 27, 23, 59, 59);
		
		LocalDateTime timePoint1Copy = LocalDateTime.of(2018, 11, 27, 23, 59, 59);
		
		assertTrue(timePoint1.equals(timePoint1Copy));
		
		LocalDateTime timePoint2 = LocalDateTime.ofInstant(is, ZoneId.systemDefault());
		
		
		assertThat(timePoint1.getSecond(), equalTo(59));
		assertThat(timePoint2.getSecond(), equalTo(59));
		
		assertThat(timePoint1.getDayOfMonth(), equalTo(27));
		assertThat(timePoint2.getDayOfMonth(), equalTo(27));
		
		assertFalse(timePoint1.equals(timePoint2));
		
		String ds = timePoint1.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssS"));
		assertThat(ds, equalTo("201811272359590"));
	}
	
	@Test
	public void tChangeLc() {
		LocalDateTime timePoint1 = LocalDateTime.of(2018, 11, 27, 23, 59, 59);
		
		LocalDateTime timePoint2 = timePoint1.withDayOfMonth(1).withHour(22);
		
		assertFalse(timePoint1 == timePoint2);
	}



}
