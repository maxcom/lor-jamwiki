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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jmwiki;

import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 */
public class WikiVersion {

	private int major = 0;
	private int minor = 0;
	private int patch = 0;
	private static WikiVersion currentVersion = null;
	private static Logger logger = Logger.getLogger(WikiVersion.class);

	static {
		currentVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
	}

	/**
	 *
	 */
	private WikiVersion(String version) {
		StringTokenizer tokens = new StringTokenizer(version, ".");
		if (tokens.countTokens() != 3) {
			// FIXME - should throw an exception
			logger.error("Invalid Wiki version: " + version);
			return;
		}
		this.major = new Integer(tokens.nextToken()).intValue();
		this.minor = new Integer(tokens.nextToken()).intValue();
		this.patch = new Integer(tokens.nextToken()).intValue();
	}

	/**
	 *
	 */
	public boolean before(int major, int minor, int patch) {
		if (this.major < major ) return true;
		if (this.major == major && this.minor < minor) return true;
		if (this.major == major && this.minor == minor && this.patch < patch) return true;
		return false;
	}

	/**
	 *
	 */
	public static WikiVersion getCurrentVersion() {
		return currentVersion;
	}

	/**
	 *
	 */
	public static void setCurrentVersion(int major, int minor, int patch) {
		currentVersion.major = major;
		currentVersion.minor = minor;
		currentVersion.patch = patch;
	}
}
