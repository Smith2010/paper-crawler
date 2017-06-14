package com.mazhen.papercrawler.util;

import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smithma on 01/06/2017.
 */
public class DataUtils {

	private DataUtils() {}

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

	public static String removeBracket(String string) {
		return StringUtils.removeEnd(StringUtils.removeStart(string, "("), ")");
	}

	public static String transformNodeList(Selectable selectable, String separator) {
		return selectable.nodes().isEmpty() ? null : StringUtils.join(selectable.all(), separator);
	}

	public static String transformNodeList(Selectable selectable, String removedSuffix, String separator) {
		List<String> list = new ArrayList<>();
		for (Selectable node : selectable.nodes()) {
			list.add(StringUtils.removeEnd(node.toString(), removedSuffix));
		}

		return list.isEmpty() ? null : StringUtils.join(list, separator);
	}

	public static String getCnkiUrlFilename(String url) {
		return StringUtils.substringBetween(url, "&filename=XFKJ", "&");
	}
}
