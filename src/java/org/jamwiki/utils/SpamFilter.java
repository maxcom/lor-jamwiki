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

import java.io.File;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 * Provide the capability for filtering content based on a predefined list of
 * regular expressions.
 */
public class SpamFilter {

	/** Logger */
	public static final WikiLogger logger = WikiLogger.getLogger(SpamFilter.class.getName());
	/** Spam blacklist file name. */
	public static final String SPAM_BLACKLIST_FILE = "spam-blacklist.txt";
	private static Pattern spamRegexPattern = null;

	/**
	 *
	 */
	public static String containsSpam(String content) throws Exception {
		long start = System.currentTimeMillis();
		if (spamRegexPattern == null) {
			SpamFilter.initialize();
		}
		Matcher m = spamRegexPattern.matcher(content);
		String result = null;
		if (m.find()) {
			result = m.group(0);
		}
		long execution = System.currentTimeMillis() - start;
		logger.fine("Executed spam filter (" + (execution / 1000.000) + " s.)");
		return result;
	}

	/**
	 *
	 */
	private static void initialize() throws Exception {
		try {
			File file = Utilities.getClassLoaderFile(SPAM_BLACKLIST_FILE);
			String regexText = FileUtils.readFileToString(file, "UTF-8");
			StringTokenizer tokens = new StringTokenizer(regexText);
			String regex = "";
			while (tokens.hasMoreTokens()) {
				regex += tokens.nextToken();
				if (tokens.hasMoreTokens()) regex += "|";
			}
			spamRegexPattern = Pattern.compile(regex);
			logger.info("Loading spam filter regular expression:" + regex);
		} catch (Exception e) {
			logger.severe("Unable to initialize spam blacklist", e);
			throw e;
		}
	}
}
