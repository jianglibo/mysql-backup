package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.go2wheel.mysqlbackup.value.PruneBackupedFiles.PathAndCt;
import com.google.common.collect.Lists;

public class TestPruneBackups {
	
	private DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

	private Calendar getCalendar() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2018);
		c.set(Calendar.MONTH, 5);
		c.set(Calendar.DAY_OF_MONTH, 5);
		c.set(Calendar.HOUR, 5);
		c.set(Calendar.MINUTE, 30);
		c.set(Calendar.SECOND, 30);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	private List<PathAndCt> fixturesInMinutes() {
		List<PathAndCt> pcts = Lists.newArrayList();
		Calendar c = getCalendar();
		for (int i = 1; i < 6; i++) {
			PathAndCt pct = new PathAndCt();
			Instant ki = c.toInstant();
			ki = ki.minusSeconds(i);
			pct.setInstant(ki);
			pcts.add(pct);
		}
		
		c.add(Calendar.MINUTE, -2);
		for (int i = 1; i < 6; i++) {
			PathAndCt pct = new PathAndCt();
			Instant ki = c.toInstant();
			ki = ki.minusSeconds(i);
			pct.setInstant(ki);
			pcts.add(pct);
		}

		Collections.sort(pcts);
		return pcts;
	}

	private List<PathAndCt> fixturesInMinutes1() {
		List<PathAndCt> pcts = Lists.newArrayList();
		Calendar c = getCalendar();
		for (int i = 1; i < 6; i++) {
			PathAndCt pct = new PathAndCt();
			Instant ki = c.toInstant();
			ki = ki.minusSeconds(i * 61);
			pct.setInstant(ki);
			pcts.add(pct);
		}
		Collections.sort(pcts);
		return pcts;
	}

	/**
	 * 以分钟分组，在分钟内保留的拷贝数就是secondly. For example. Today is, 2013-10-1 08:30:31
	 * 2013-10-1 08:30:32 2013-10-1 08:30:33 2013-10-1 08:30:34 2013-10-2 08:30:31
	 * 2013-10-2 08:30:32 2013-10-2 08:30:33 2013-10-2 08:30:34 The map has two
	 * entries. The two keys are �?2013-10-1 08:30" and "2013-10-2 08:30". Each has
	 * value of 4 items list. If we keep secondly to 2 then 2 early item will be
	 * deleted. what's remains? 2013-10-1 08:30:33 2013-10-1 08:30:34 2013-10-2
	 * 08:30:33 2013-10-2 08:30:34 keep going on, If the hourly is 1. then the
	 * result of byhours is: 2013-10-1 08 2013-10-2 08
	 * 
	 * 删除总是从第二个�?始�??
	 */
	@Test
	public void tSencondly() {
		PruneBackupedFiles pbf = new PruneBackupedFiles(Paths.get(""));
		Map<Instant, List<PathAndCt>> m = pbf.prune(fixturesInMinutes(), 0, 2, 0, 0, 0, 0, 0);
		assertThat(m.size(), equalTo(2));
		Iterator<List<PathAndCt>> it = m.values().iterator();
		List<PathAndCt> l1 = it.next();
		assertThat(l1.size(), equalTo(5));
		List<PathAndCt> l2 = it.next();
		assertThat(l2.size(), equalTo(1));
		String is = l2.get(0).getInstant().toString();
		assertThat(is, equalTo("2018-06-05T09:30:29Z")); // the latest is keeped.
	}

	@Test
	public void tSencondly1() {
		PruneBackupedFiles pbf = new PruneBackupedFiles(Paths.get(""));
		Map<Instant, List<PathAndCt>> m = pbf.byMinutes(fixturesInMinutes1(), 1);
		assertThat(m.size(), equalTo(5));
		assertThat(m.values().iterator().next().size(), equalTo(1));
	}

}
