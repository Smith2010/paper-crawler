package com.mazhen.papercrawler.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by smithma on 01/06/2017.
 */
public class DataUtils {

	public static String transformNumber(String number) {
		if (StringUtils.isBlank(number)) {
			return "0";
		} else if (StringUtils.endsWith(number, "k")) {
			Double real = Double.valueOf(StringUtils.removeEnd(number, "k")) * 1000;
			return String.valueOf(real.longValue());
		} else {
			return number;
		}
	}
}
