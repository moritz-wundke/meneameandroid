package com.dcg.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Based on Romain Guys TextUtilities from the shelves app
 * (http://code.google.com/p/shelves/)
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public final class TextUtilities {
	private static final String SEPARATOR = ";";

	private TextUtilities() {
	}

	public static String join(Collection<?> items, String delimiter) {
		String finalDelimiter = SEPARATOR;
		if (items == null || items.isEmpty()) {
			return "";
		}

		if (delimiter.compareTo("") != 0) {
			finalDelimiter = delimiter;
		}

		final Iterator<?> iter = items.iterator();
		final StringBuilder buffer = new StringBuilder(iter.next().toString());

		while (iter.hasNext()) {
			buffer.append(finalDelimiter).append(iter.next());
		}

		return buffer.toString();
	}
}
