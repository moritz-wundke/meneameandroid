package com.dcg.rss;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class RSSParserStopRequestException extends SAXParseException {
	private static final long serialVersionUID = 7384883373438844693L;

	// Max elements reached
	public RSSParserStopRequestException(String message, Locator locator) {
		super(message, locator);
	}
}
