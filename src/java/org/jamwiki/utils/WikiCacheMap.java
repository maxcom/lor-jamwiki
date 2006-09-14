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
package org.jamwiki.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jamwiki.WikiLogger;

/**
 * This class provides a utility class useful for storing a cache of objects.
 * It extends the java.util.LinkedHashMap class and provides the capability to
 * limit the cache size to a specific number of elements, and automatically
 * purges the objects that were accessed least recently when adding new objects.
 */
public class WikiCacheMap extends LinkedHashMap {

	/** Logger */
	public static final WikiLogger logger = WikiLogger.getLogger(WikiCacheMap.class.getName());
	/** The maximum number of elements that can be stored in this map. */
	private int maxSize = 0;

	/**
	 *
	 */
	public WikiCacheMap(int maxSize) {
		// invoke parent constructor with default values, and indicate that
		// least recently accessed elements should be purged first.
		super(16, (float)0.75, true);
		this.maxSize = maxSize;
	}

	/**
	 *
	 */
	protected boolean removeEldestEntry(Map.Entry eldest) {
		// if maxSize not initialized assume no maximum
		if (this.maxSize <= 0) return false;
		// if current size greater than max size, purge least-recently accessed
		return size() > this.maxSize;
	}
}
