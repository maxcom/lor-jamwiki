/*
 * $Id: PrintableEntry.java 644 2006-04-23 07:52:28Z wrh2 $
 *
 * Filename  : PrintableEntry.java
 * Project   : VQWiki
 * Author	: Tobias Schulz-Hess (sourcefoge@schulz-hess.de)
 */
package org.jmwiki;


/**
 * One entry of the printable page (the page can contain multiple entries)
 *
 * This class was created on 20:18:49 15.04.2003
 *
 * @author Tobias Schulz-Hess (sourcefoge@schulz-hess.de)
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

/*
 * Log:
 *
 * $Log$
 * Revision 1.4  2006/04/23 07:52:28  wrh2
 * Coding style updates (VQW-73).
 *
 * Revision 1.3  2003/10/05 05:07:30  garethc
 * fixes and admin file encoding option + merge with contributions
 *
 * Revision 1.2  2003/04/15 23:11:01  garethc
 * lucene fixes
 *
 * Revision 1.1  2003/04/15 18:40:25  mrgadget4711
 * ADD: Print multiple pages
 *
 * ------------END------------
 */
