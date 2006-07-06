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
package org.jamwiki.parser;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Returns general informations about the parser.
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
