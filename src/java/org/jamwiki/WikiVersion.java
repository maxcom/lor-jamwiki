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
import org.springframework.util.StringUtils;

/**
 *
 */
public class WikiVersion {

	private static WikiLogger logger = WikiLogger.getLogger(WikiVersion.class.getName());
	private int major = 0;
	private int minor = 0;
	private int patch = 0;

	/** Current software version.  If this differs from the version in the properties an upgrade is performed. */
	public final static String CURRENT_WIKI_VERSION = "0.3.4";

	/**
	 *
	 */
	public WikiVersion(String version) {
		if (!StringUtils.hasText(version)) {
			// FIXME - should throw an exception
			logger.severe("Invalid Wiki version: " + version);
			return;
		}
		StringTokenizer tokens = new StringTokenizer(version, ".");
		if (tokens.countTokens() != 3) {
			// FIXME - should throw an exception
			logger.severe("Invalid Wiki version: " + version);
			return;
		}
		this.major = new Integer(tokens.nextToken()).intValue();
		this.minor = new Integer(tokens.nextToken()).intValue();
		this.patch = new Integer(tokens.nextToken()).intValue();
	}

	/**
	 *
	 */
	public boolean before(WikiVersion version) {
		return this.before(version.major, version.minor, version.patch);
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
}
