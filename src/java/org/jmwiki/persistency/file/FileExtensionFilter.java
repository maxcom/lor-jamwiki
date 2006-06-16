package org.jmwiki.persistency.file;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author garethc
 * Date: 5/03/2003
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
