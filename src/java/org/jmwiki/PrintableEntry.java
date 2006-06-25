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

/**
 * One entry of the printable page (the page can contain multiple entries)
 */
public class PrintableEntry {

	/** The topic of this entry */
	private String topic;
	/** The content of this entry */
	private String content;

	/**
	 * Get the topic
	 *
	 * @return topic as String
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * Get the content
	 *
	 * @return content as String
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Set the topic
	 *
	 * @param mytopic New topic
	 */
	public void setTopic(String mytopic) {
		topic = mytopic;
	}

	/**
	 * Set the content
	 *
	 * @param mycontent New content
	 */
	public void setContent(String mycontent) {
		content = mycontent;
	}
}
