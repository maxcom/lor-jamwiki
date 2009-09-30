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
package org.jamwiki;

import java.io.File;
import java.io.FileReader;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 *
 */
public class TestFileUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(TestFileUtil.class.getName());
	public static final String TEST_TOPICS_DIR = "data/topics/";
	public static final String TEST_RESULTS_DIR = "data/results/";

	/**
	 *
	 */
	private static File retrieveFile(String directory, String fileName) {
		// files containing colons aren't allowed, so replace with "_-_"
		String fullName = directory + StringUtils.replace(fileName, ":", "_-_");
		File file = null;
		try {
			return Utilities.getClassLoaderFile(fullName);
		} catch (Exception e) { }
		try {
			return new File(Utilities.getClassLoaderRoot(), fullName);
		} catch (Exception e) { }
		return null;
	}

	/**
	 *
	 */
	public static String retrieveFileContent(String directory, String fileName) throws Exception {
		FileReader reader = null;
		try {
			File file = TestFileUtil.retrieveFile(directory, fileName);
			if (file == null) {
				return null;
			}
			reader = new FileReader(file);
			StringBuffer output = new StringBuffer();
			char[] buf = new char[4096];
			int c;
			while ((c = reader.read(buf, 0, buf.length)) != -1) {
				output.append(buf, 0, c);
			}
			return output.toString();
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (Exception e) { }
			}
		}
	}
}
