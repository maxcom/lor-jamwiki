/*
 * $Id: SelectorBean.java 635 2006-04-19 23:55:16Z wrh2 $
 *
 * Filename  : SelectorBean.java
 * Created   : 24.06.2004
 * Project   : VQWiki
 */
package org.jmwiki.servlets.beans;

/**
 * Bean, which is used for a selector
 *
 * @version $Revision: 635 $ - $Date: 2006-04-20 01:55:16 +0200 (do, 20 apr 2006) $
 * @author SinnerSchrader (tobsch)
 */
public class SelectorBean {

	private String key;
	private String label;

	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param key The key to set.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
