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
package org.jmwiki.users;

/**
 * Bean, which is used for a selector
 */
public class SelectorBean {

	private String key;

	private String label;

	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param key The key to set.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
