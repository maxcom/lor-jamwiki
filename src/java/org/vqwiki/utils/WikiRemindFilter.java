package org.vqwiki.utils;

import java.io.File;

/**
 * The FilenameFilter for Reminder files: *.rmd
 *
 * @author Robert E Brewer
 * @version 0.1
 */
public class WikiRemindFilter implements java.io.FilenameFilter {

	/**
	 *
	 */
	public boolean accept(File dir, String name) {
		return name.endsWith(".rmd");
	}
}
