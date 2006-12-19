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

import java.util.StringTokenizer;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 * Utility class that holds the current Wiki version constant and provides
 * methods for comparing Wiki versions.
 */
public class WikiVersion {

	private static WikiLogger logger = WikiLogger.getLogger(WikiVersion.class.getName());
	private final int major;
	private final int minor;
	private final int patch;

	/** Current software version.  If this differs from the version in the properties an upgrade is performed. */
	public final static String CURRENT_WIKI_VERSION = "0.5.0";

	/**
	 * Constructor to create a new Wiki version object using a version string of
	 * the form "0.3.5".
	 *
	 * @param version A version string of the form "0.3.5".
	 */
	public WikiVersion(String version) throws Exception {
		if (!StringUtils.hasText(version)) {
			throw new Exception("Invalid Wiki version: " + version);
		}
		StringTokenizer tokens = new StringTokenizer(version, ".");
		if (tokens.countTokens() != 3) {
			throw new Exception("Invalid Wiki version: " + version);
		}
		this.major = new Integer(tokens.nextToken()).intValue();
		this.minor = new Integer(tokens.nextToken()).intValue();
		this.patch = new Integer(tokens.nextToken()).intValue();
	}

	/**
	 * Utility method for comparing the current Wiki version with another
	 * version.
	 *
	 * @param version A Wiki version to compare against.
	 * @return Returns <code>true</code> if the current version is older than
	 *  the version being compared against.
	 */
	public boolean before(WikiVersion version) {
		return this.before(version.major, version.minor, version.patch);
	}

	/**
	 * Utility method for comparing the current Wiki version with another
	 * version.
	 *
	 * @param major The major version number to compare against.
	 * @param minor The minor version number to compare against.
	 * @param patch The patch level to compare against.
	 * @return Returns <code>true</code> if the current version is older than
	 *  the version being compared against.
	 */
	public boolean before(int major, int minor, int patch) {
		if (this.major < major) {
			return true;
		}
		if (this.major == major && this.minor < minor) {
			return true;
		}
		if (this.major == major && this.minor == minor && this.patch < patch) {
			return true;
		}
		return false;
	}
}
