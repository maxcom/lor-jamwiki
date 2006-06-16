package org.jmwiki.parser;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Returns general informations about the parser.
 * @author boessu
 */
public class ParserInfo {

	protected final String name;
	protected final String version;
	protected final String bundleName;

	public ParserInfo(String name, String version, String bundleName) {
		this.name = name;
		this.version = version;
		this.bundleName = bundleName;
	}

	/**
	 * @return name of the parser
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param loc localization for the language to use
	 * @return short description of the parser
	 */
	public String getDescription(Locale loc) {
		return ResourceBundle.getBundle(bundleName, loc).getString("parser.description");
	}

	/**
	 * @return version of the parser.
	 */
	public String getVersion() {
		return version;
	}
}
