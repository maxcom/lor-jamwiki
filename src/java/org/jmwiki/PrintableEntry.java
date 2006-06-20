/*
 *
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
