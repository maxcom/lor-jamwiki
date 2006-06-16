/*
 * Filename  : SelectorBean.java
 * Created   : 24.06.2004
 * Project   : VQWiki
 */
package org.jmwiki.users;

/**
 * Bean, which is used for a selector
 *
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
