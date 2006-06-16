/*
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
package org.jmwiki;

import java.util.List;
import java.util.Date;

public interface VersionManager {

	/**
	 * Returns the revision key to get topic with
	 * Revision 0 is the most recent revision
	 */
	public Object lookupRevision(String virtualWiki, String topicName, int version) throws Exception;

	/**
	 *
	 */
	public String diff(String virtualWiki, String topicName, int revision1, int revision2, boolean useHtml) throws Exception;

	/**
	 *
	 */
	public java.util.Date lastRevisionDate(String virtualWiki, String topicName) throws Exception;

	/**
	 * Returns all versions of the given topic in reverse chronological order
	 * @param virtualWiki
	 * @param topicName
	 * @return
	 * @throws Exception
	 */
	public List getAllVersions(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public TopicVersion getTopicVersion(String virtualWiki, String topicName, int versionNumber) throws Exception;

	/**
	 *
	 */
	public String getVersionContents(String virtualWiki, String topicName, int versionNumber) throws Exception;

	/**
	 *
	 */
	public int getNumberOfVersions(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	void addVersion(String virtualWiki, String topicName, String contents, Date at) throws Exception;
}
