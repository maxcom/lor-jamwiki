/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * This class extends the {@link java.util.logging.Formatter} class to format
 * log messages.
 *
 * @see org.jamwiki.utils.WikiLogger
 */
public class WikiLogFormatter extends Formatter {

	private static final String SEPARATOR = System.getProperty("line.separator");
	private final String datePattern;

	/**
	 *
	 */
	public WikiLogFormatter(String datePattern) {
		super();
		this.datePattern = datePattern;
	}

	/**
	 *
	 */
	public String format(LogRecord record) {
		StringBuffer buffer = new StringBuffer();
		SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
		buffer.append(formatter.format(new Date(record.getMillis())));
		buffer.append(' ');
		buffer.append(record.getLevel().getName());
		buffer.append(": ");
		buffer.append(record.getLoggerName());
		buffer.append(" - ");
		buffer.append(formatMessage(record));
		buffer.append(SEPARATOR);
		Throwable throwable = record.getThrown();
		if (throwable != null) {
			StringWriter sw = new StringWriter();
			throwable.printStackTrace(new PrintWriter(sw, true));
			buffer.append(sw.toString());
		}
		return buffer.toString();
	}
}
