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

import java.io.File;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.jamwiki.Environment;

/**
 *
 */
public class WikiCache {

	private static WikiLogger logger = WikiLogger.getLogger(WikiCache.class.getName());
	private static CacheManager cacheManager = null;

	/** Directory for cache files. */
	private static final String CACHE_DIR = "cache";

	static {
		WikiCache.initialize();
	}

	/**
	 *
	 */
	public static void addToCache(String cacheName, Object key, Object value) {
		Cache cache = WikiCache.getCache(cacheName);
		cache.put(new Element(key, value));
	}

	/**
	 *
	 */
	public static void addToCache(String cacheName, int key, Object value) {
		WikiCache.addToCache(cacheName, new Integer(key), value);
	}

	/**
	 *
	 */
	private static Cache getCache(String cacheName) {
		if (!WikiCache.cacheManager.cacheExists(cacheName)) {
			int maxSize = Environment.getIntValue(Environment.PROP_CACHE_INDIVIDUAL_SIZE);
			int maxAge = Environment.getIntValue(Environment.PROP_CACHE_MAX_AGE);
			int maxIdleAge = Environment.getIntValue(Environment.PROP_CACHE_MAX_IDLE_AGE);
			Cache cache = new Cache(cacheName, maxSize, true, false, maxAge, maxIdleAge);
			WikiCache.cacheManager.addCache(cache);
		}
		return WikiCache.cacheManager.getCache(cacheName);
	}

	/**
	 *
	 */
	public static void initialize() {
		try {
			if (WikiCache.cacheManager != null) {
				WikiCache.cacheManager.removalAll();
				WikiCache.cacheManager.shutdown();
			}
			File directory = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), CACHE_DIR);
			if (!directory.exists()) {
				directory.mkdir();
			}
			Configuration configuration = new Configuration();
			CacheConfiguration defaultCacheConfiguration = new CacheConfiguration();
			defaultCacheConfiguration.setDiskPersistent(false);
			defaultCacheConfiguration.setEternal(false);
			defaultCacheConfiguration.setOverflowToDisk(true);
			defaultCacheConfiguration.setMaxElementsInMemory(Environment.getIntValue(Environment.PROP_CACHE_TOTAL_SIZE));
			defaultCacheConfiguration.setName("defaultCache");
			configuration.addDefaultCache(defaultCacheConfiguration);
			DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
//			diskStoreConfiguration.addExpiryThreadPool(new ThreadPoolConfiguration("", new Integer(5), new Integer(5)));
//			diskStoreConfiguration.addSpoolThreadPool(new ThreadPoolConfiguration("", new Integer(5), new Integer(5)));
			diskStoreConfiguration.setPath(directory.getPath());
			configuration.addDiskStore(diskStoreConfiguration);
			WikiCache.cacheManager = new CacheManager(configuration);
		} catch (Exception e) {
			logger.severe("Initialization error in WikiCache", e);
		}
	}

	/**
	 *
	 */
	public static String key(String virtualWiki, String topicName) {
		return virtualWiki + "/" + topicName;
	}

	/**
	 *
	 */
	public static void removeCache(String cacheName) {
		WikiCache.cacheManager.removeCache(cacheName);
	}

	/**
	 *
	 */
	public static void removeFromCache(String cacheName, Object key) {
		Cache cache = WikiCache.getCache(cacheName);
		cache.remove(key);
	}

	/**
	 *
	 */
	public static void removeFromCache(String cacheName, int key) {
		WikiCache.removeFromCache(cacheName, new Integer(key));
	}

	/**
	 * Retrieve a cached element from the cache.  This method will return
	 * <code>null</code> if no matching element is cached, an element with
	 * no value if a <code>null</code> value is cached, or an element with a
	 * valid object value if such an element is cached.
	 */
	public static Element retrieveFromCache(String cacheName, Object key) {
		Cache cache = WikiCache.getCache(cacheName);
		return cache.get(key);
	}

	/**
	 *
	 */
	public static Element retrieveFromCache(String cacheName, int key) {
		return WikiCache.retrieveFromCache(cacheName, new Integer(key));
	}
}
