package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;

import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.Server;


public class TestObjectUtil {
	
	@Test
	public void tDump() {
		Ot ot = new Ot();
		
		String s = ObjectUtil.dumpObjectAsMap(ot);
		assertThat(s, equalTo("ii: 33"));
	}
	
	@Test
	public void tFields() {
		List<Field> fds = ObjectUtil.getFields(Server.class); 
		assertThat(fds.size(), equalTo(12));
	}
	
	@Test
	public void tSetInt() throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		Ot ot = new Ot();
		Field fd = ObjectUtil.getField(Ot.class, "ii").get();
		
		ObjectUtil.setValue(fd, ot, "20");
		assertThat(ot.getIi(), equalTo(20));
	}


	
	public static class Ot {
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
