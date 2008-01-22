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
package org.jamwiki.model;

import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a configuration value as used by
 * {@link org.jamwiki.WikiConfiguration}.
 */
public class WikiConfigurationObject {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(WikiConfigurationObject.class.getName());

	private String clazz;
	private String key;
	private String name;
	private String state;

	/**
	 *
	 */
	public String getClazz() {
		return this.clazz;
	}

	/**
	 *
	 */
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	/**
	 *
	 */
	public boolean isExperimental() {
		return (this.state != null && this.state.equalsIgnoreCase("experimental"));
	}

	/**
	 *
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 *
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 *
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 */
	public String getState() {
		return this.state;
	}

	/**
	 *
	 */
	public void setState(String state) {
		this.state = state;
	}
}
