/**
 *
 */
package org.jmwiki.persistency.file;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 */
public class FileExtensionFilter implements FilenameFilter {

	String extension;

	/**
	 *
	 */
	public FileExtensionFilter(String extension) {
		this.extension = extension;
	}

	/**
	 *
	 */
	public boolean accept(File file, String name) {
		return (name.endsWith(extension));
	}
}
