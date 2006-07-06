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
package org.jamwiki.persistency.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.search.AbstractSearchEngine;
import org.springframework.util.StringUtils;

/**
 *
 */
public class DatabaseSearchEngine extends AbstractSearchEngine {

	/** An instance to myself (Singleton pattern) */
	protected static DatabaseSearchEngine instance;

	/**
	 * Creates a new DatabaseSearchEngine object.
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	private DatabaseSearchEngine() throws Exception {
	}

	/**
	 * Get the one and only existing instance of this class
	 *
	 * @return An Instance to this class
	 *
	 * @throws Exception If something really goes wrong
	 */
	public static synchronized DatabaseSearchEngine getInstance() throws Exception {
		String baseFileDir = Environment.getValue(Environment.PROP_BASE_FILE_DIR);
		if (!StringUtils.hasText(baseFileDir)) {
			// system not initialized yet
			return null;
		}
		if (instance == null) {
			instance = new DatabaseSearchEngine();
			instance.initSearchEngine();
			instance.refreshIndex();
		}
		return instance;
	}

	/**
	 * Get the filename of a topic file.
	 * @see jamwiki.AbstractSearchEngine#getFilename(java.lang.String, java.lang.String)
	 */
	protected String getFilename(String currentWiki, String topic) {
		return null;
	}

}
