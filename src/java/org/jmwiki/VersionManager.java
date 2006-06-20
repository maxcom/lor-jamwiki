/**
 *
 */
package org.jmwiki;

import java.util.List;
import java.util.Date;
import org.jmwiki.model.TopicVersion;

/**
 *
 */
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
	public TopicVersion getTopicVersion(String context, String virtualWiki, String topicName, int versionNumber) throws Exception;

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
