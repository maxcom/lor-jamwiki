package org.jmwiki.persistency;

import org.jmwiki.persistency.db.DBDate;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 Java MediaWiki - WikiWikiWeb clone
 Copyright (C) 2001-2002 Gareth Cronin

 This program is free software; you can redistribute it and/or modify
 it under the terms of the latest version of the GNU Lesser General
 Public License as published by the Free Software Foundation;

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program (gpl.txt); if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

public interface PersistencyHandler {

	/**
	 *
	 */
	public String read(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public void write(String virtualWiki, String contents, String topicName) throws Exception;

	/**
	 *
	 */
	public boolean holdsLock(String virtualWiki, String topicName, String key) throws Exception;

	/**
	 *
	 */
	public boolean lockTopic(String virtualWiki, String topicName, String key) throws Exception;

	/**
	 *
	 */
	public void unlockTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public boolean isTopicReadOnly(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public Collection getReadOnlyTopics(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public void addReadOnlyTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public void removeReadOnlyTopic(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public boolean exists(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public Collection getVirtualWikiList() throws Exception;

	/**
	 *
	 */
	public Collection getTemplateNames(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public String getTemplate(String virtualWiki, String templateName) throws Exception;

	/**
	 *
	 */
	public void addVirtualWiki(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public Collection purgeDeletes(String virtualWiki) throws Exception;

	/**
	 *
	 */
	public void purgeVersionsOlderThan(String virtualWiki, DBDate date) throws Exception;

	/**
	 *
	 */
	public void saveAsTemplate(String virtualWiki, String templateName, String contents) throws Exception;

	/**
	 *
	 */
	public List getLockList(String virtualWiki) throws Exception;
}
