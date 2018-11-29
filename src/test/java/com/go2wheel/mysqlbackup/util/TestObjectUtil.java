package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.value.Server;


public class TestObjectUtil {
	
	@Test
	public void tDump() {
		Ot ot = new Ot();
		
		String s = ObjectUtil.dumpObjectAsMap(ot);
		assertTrue(s.startsWith("ii: 33"));
		assertTrue(s.contains("createdAt"));
		
		
		s = ObjectUtil.toListRepresentation(ot, "ii", "createdAt");
		assertTrue(s.contains("ii: 33"));
		assertTrue(s.contains("createdAt"));

	}
	
	@Test
	public void tFields() {
		List<Field> fds = ObjectUtil.getFields(Server.class); 
		assertThat(fds.size(), equalTo(15));
	}
	
	@Test
	public void tSetInt() throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		Ot ot = new Ot();
		Field fd = ObjectUtil.getField(Ot.class, "ii").get();
		
		ObjectUtil.setValue(fd, ot, "20");
		assertThat(ot.getIi(), equalTo(20));
	}

	
	public static class OtSuper {
		private Date createdAt = new Date();

		public Date getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(Date createdAt) {
			this.createdAt = createdAt;
		}
	}

	
	public static class Ot extends OtSuper{
		private static int si = 55;
		
		private int ii = 33;
		
		public int getVi() {
			return 44;
		}

		public int getIi() {
			return ii;
		}

		public void setIi(int ii) {
			this.ii = ii;
		}
		
		
	}

}
