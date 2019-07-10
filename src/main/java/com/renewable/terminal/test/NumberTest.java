package com.renewable.terminal.test;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description：
 * @Author: jarry
 */
public class NumberTest {
	public static void main(String[] args) {
		String str = "6.0000000e-02";
		String str2 = " 5.6515604e+01";

		System.out.println(StringUtils.isNumericSpace(str));
		System.out.println(Double.valueOf(str));
		System.out.println(Double.parseDouble(str));
	}
}
