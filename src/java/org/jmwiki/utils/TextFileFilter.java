/**
 *
 */
package org.jmwiki.utils;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 */
public class TextFileFilter implements FilenameFilter {

	/**
	 *
	 */
	public TextFileFilter() {
	}

	/**
	 *
	 */
	public boolean accept(File file, String name) {
		if (name.endsWith(".txt")) return true;
		return false;
	}
}
