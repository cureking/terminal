package com.renewable.terminal.test;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
public class NullTest {
	public static void main(String[] args) {
		List<String> strList = Lists.newArrayList();
		strList.add("test");
		strList.add("");
		strList.add("");
		strList.add(null);
//		strList.removeIf(Objects::isNull);
//		strList.removeIf(e -> e.isEmpty());
		strList.removeIf(e -> e == null || e.isEmpty());
//		strList = strList.stream().filter(string -> !string.isEmpty()).collect(Collectors.toList());
//		strList = strList.stream()
//				.filter(e -> e.isEmpty())
//				.collect(Collectors.toList());

		System.out.println(strList);
	}
}
