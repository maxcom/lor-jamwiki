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
package org.jamwiki.persistency;

import java.io.File;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Category;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.persistency.db.DatabaseHandler;
import org.jamwiki.persistency.file.FileHandler;
import org.jamwiki.utils.DiffUtil;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiCacheMap;
import org.springframework.util.StringUtils;

/**
 *
 */
public abstract class PersistencyHandler {

	private static WikiLogger logger = WikiLogger.getLogger(PersistencyHandler.class.getName());
}
