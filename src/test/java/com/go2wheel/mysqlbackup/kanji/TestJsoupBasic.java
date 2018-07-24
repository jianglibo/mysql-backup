package com.go2wheel.mysqlbackup.kanji;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class TestJsoupBasic {
	
	private Path fixture = Paths.get("fixtures", "List of jōyō kanji - Wikipedia.html");
	
	@Test
	public void t() throws IOException {
		Document doc = Jsoup.parse(fixture.toFile(), StandardCharsets.UTF_8.name());
		Elements trs = doc.select("table.sortable.wikitable.jquery-tablesorter tbody tr");
		for (Element tr : trs) {
			Elements tds =tr.select("td");
			for (Element td : tds) {
				assertThat(tds.size(), equalTo(9));
				System.out.println(td.text());
//				System.out.println(td.html());
			}
		}
	}

}
