/*
 * $Id: SearchResultEntry.java 644 2006-04-23 07:52:28Z wrh2 $
 *
 * Filename  : SearchResultEntry.java
 * Project   : VQWiki
 * Auhtor	: Tobias Schulz-Hess (sourceforge@schulz-hess.de)
 */
package org.jmwiki;

/**
 * bean, which contains one search result entry
 *
 * This class was created on 09:58:54 14.04.2003
 *
 * @author Tobias Schulz-Hess (sourceforge@schulz-hess.de)
 */
public class SearchResultEntry {

	/** The topic of this entry */
	private String topic = "";
	/** Text before found text */
	private String textBefore = "";
	/** the found word */
	private String foundWord = "";
	/** the text after the found word */
	private String textAfter = "";
	/** the hit ranking */
	private float ranking = 0.0f;

	/**
	 * @return
	 */
	public String getFoundWord() {
		return foundWord;
	}

	/**
	 * @return
	 */
	public float getRanking() {
		return ranking;
	}

	/**
	 * @return
	 */
	public String getTextAfter() {
		return textAfter;
	}

	/**
	 * @return
	 */
	public String getTextBefore() {
		return textBefore;
	}

	/**
	 * @return
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param string
	 */
	public void setFoundWord(String string) {
		foundWord = string;
	}

	/**
	 * @param f
	 */
	public void setRanking(float f) {
		ranking = f;
	}

	/**
	 * @param string
	 */
	public void setTextAfter(String string) {
		textAfter = string;
	}

	/**
	 * @param string
	 */
	public void setTextBefore(String string) {
		textBefore = string;
	}

	/**
	 * @param string
	 */
	public void setTopic(String string) {
		topic = string;
	}

	/**
	 * Equals
	 * @param o the other object
	 * @return equal o
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SearchResultEntry)) return false;
		final SearchResultEntry searchResultEntry = (SearchResultEntry) o;
		if (ranking != searchResultEntry.ranking) return false;
		if (foundWord != null ? !foundWord.equals(searchResultEntry.foundWord) : searchResultEntry.foundWord != null) return false;
		if (textAfter != null ? !textAfter.equals(searchResultEntry.textAfter) : searchResultEntry.textAfter != null) return false;
		if (textBefore != null ? !textBefore.equals(searchResultEntry.textBefore) : searchResultEntry.textBefore != null) return false;
		if (topic != null ? !topic.equals(searchResultEntry.topic) : searchResultEntry.topic != null) return false;
		return true;
	}

	/**
	 * Hashcode
	 * @return hashcode
	 */
	public int hashCode() {
		int result;
		result = (topic != null ? topic.hashCode() : 0);
		result = 29 * result + (textBefore != null ? textBefore.hashCode() : 0);
		result = 29 * result + (foundWord != null ? foundWord.hashCode() : 0);
		result = 29 * result + (textAfter != null ? textAfter.hashCode() : 0);
		result = 29 * result + Float.floatToIntBits(ranking);
		return result;
	}
}

/*
 * Log:
 *
 * $Log$
 * Revision 1.4  2006/04/23 07:52:28  wrh2
 * Coding style updates (VQW-73).
 *
 * Revision 1.3  2004/02/28 04:05:42  garethc
 * General bug fixes, panic on admin console
 *
 * Revision 1.2  2003/04/15 23:11:02  garethc
 * lucene fixes
 *
 * Revision 1.1  2003/04/15 08:41:32  mrgadget4711
 * ADD: Lucene search
 * ADD: RSS Stream
 *
 * ------------END------------
 */