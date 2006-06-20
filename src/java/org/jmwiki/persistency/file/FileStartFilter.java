/**
 *
 */
package org.jmwiki.persistency.file;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 */
public class FileStartFilter implements FilenameFilter {

	String prefix;

	/**
	 *
	 */
	public FileStartFilter(String prefix) {
		this.prefix = prefix;
	}

	/**
	 *
	 */
	public boolean accept(File file, String name) {
		return (name.startsWith(prefix));
	}
}