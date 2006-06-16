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
