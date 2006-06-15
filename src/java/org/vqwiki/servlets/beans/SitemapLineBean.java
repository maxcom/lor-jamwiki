/*
Very Quick Wiki - WikiWikiWeb clone
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
package org.vqwiki.servlets.beans;

import java.util.List;

/**
 * Bean, which represents the information of one line in the sitemap.
 *
 * This class was created on 20:34:52 20.07.2003
 *
 * @author $Author: wrh2 $
 */
public class SitemapLineBean {

	private String topic;
	private List levels;
	private String group;
	private boolean hasChildren;

	/**
	 * @return
	 */
	public List getLevels() {
		return levels;
	}

	/**
	 * @return
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param list
	 */
	public void setLevels(List list) {
		levels = list;
	}

	/**
	 * @param string
	 */
	public void setTopic(String string) {
		topic = string;
	}

	/**
	 * @return
	 */
	public boolean isHasChildren() {
		return hasChildren;
	}

	/**
	 * @return
	 */
	public boolean getHasChildren() {
		return hasChildren;
	}

	/**
	 * @param b
	 */
	public void setHasChildren(boolean b) {
		hasChildren = b;
	}

	/**
	 * @return
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param string
	 */
	public void setGroup(String string) {
		group = string;
	}
}

/*
 * Log:
 *
 * $Log$
 * Revision 1.4  2006/04/19 23:55:16  wrh2
 * Coding style updates (VQW-73).
 *
 * Revision 1.3  2003/10/05 05:07:32  garethc
 * fixes and admin file encoding option + merge with contributions
 *
 * Revision 1.2  2003/07/21 20:58:34  mrgadget4711
 * ADD: Dynamically open / close subtrees in IE (using DHTML)
 *
 * Revision 1.1  2003/07/20 20:34:40  mrgadget4711
 * ADD: Sitemap
 *
 * ------------END------------
 */