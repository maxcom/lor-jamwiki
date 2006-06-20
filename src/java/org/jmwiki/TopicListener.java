/**
 *
 */
package org.jmwiki;

import java.util.EventListener;

/**
 * Interface for listening for topic changes
 */
public interface TopicListener extends EventListener {

	/**
	 * Fired when a topic is saved
	 * @param event event with information
	 */
	public void topicSaved(TopicSavedEvent event);
}
