package com.dcg.rss;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Exception raised by our RSS parser when the max number of elements has been
 * reached
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class RSSParserMaxElementsException extends SAXParseException {
	private static final long serialVersionUID = -8652296316189269999L;

	// Max elements reached
	public RSSParserMaxElementsException(String message, Locator locator) {
		super(message, locator);
	}
}
