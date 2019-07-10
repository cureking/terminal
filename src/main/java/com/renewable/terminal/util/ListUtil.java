package com.renewable.terminal.util;

import java.util.List;
import java.util.Objects;

/**
 * @Description：
 * @Author: jarry
 */
public class ListUtil {

//	public <T> T genericMethod(Class<T> tClass)

	public static List<String> removeNullByStringList(List<String> stringList) {
		stringList.removeIf(Objects::isNull);
		return stringList;
	}

	public static List<String> removeEmptyByStringList(List<String> stringList) {
		stringList.removeIf(Objects::isNull);
//		strList = strList.stream().filter(string -> !string.isEmpty()).collect(Collectors.toList());
//		strList = strList.stream().filter(e -> e.isNotEmpty()).collect(Collectors.toList());
		stringList.removeIf(str -> str.isEmpty());
		return stringList;
	}

	public static List<String> removeEmptyByStringListOld(List<String> stringList) {
		for (String str : stringList) {
			if (str == null || str.isEmpty()) {
				stringList.remove(str);
			}
		}
		return stringList;
	}


}
