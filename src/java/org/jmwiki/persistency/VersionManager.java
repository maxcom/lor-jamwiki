/**
 *
 */
package org.jmwiki.persistency;

import java.util.List;
import java.util.Date;
import org.jmwiki.model.TopicVersion;

/**
 *
 */
public interface VersionManager {

	/**
	 *
	 */
	public String diff(String virtualWiki, String topicName, int revision1, int revision2, boolean useHtml) throws Exception;

	/**
	 *
	 */
	public java.util.Date lastRevisionDate(String virtualWiki, String topicName) throws Exception;

	/**
	 *
	 */
	public TopicVersion getTopicVersion(String context, String virtualWiki, String topicName, int topicVersionId) throws Exception;

	/**
	 *
	 */
	public String getVersionContents(String virtualWiki, String topicName, int topicVersionId) throws Exception;

	/**
	 *
	 */
	public int getNumberOfVersions(String virtualWiki, String topicName) throws Exception;
}
