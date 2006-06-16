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
package org.jmwiki.servlets.beans;


/**
 * Bean containing results for a statistic page
 *
 * This class was created on 09:34:30 19.07.2003
 *
 * @author $Author: wrh2 $
 */
public class StatisticsMonthBean {

	private String name = null;
	private String changes = null;
	private String pages = null;
	private String ratio = null;

	/**
	 * @return
	 */
	public String getChanges() {
		return changes;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param string
	 */
	public void setChanges(String string) {
		changes = string;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @return
	 */
	public String getPages() {
		return pages;
	}

	/**
	 * @return
	 */
	public String getRatio() {
		return ratio;
	}

	/**
	 * @param string
	 */
	public void setPages(String string) {
		pages = string;
	}

	/**
	 * @param string
	 */
	public void setRatio(String string) {
		ratio = string;
	}
}

/*
 * Log:
 *
 * $Log$
 * Revision 1.3  2006/04/19 23:55:16  wrh2
 * Coding style updates (VQW-73).
 *
 * Revision 1.2  2003/10/05 05:07:32  garethc
 * fixes and admin file encoding option + merge with contributions
 *
 * Revision 1.1  2003/07/19 13:22:59  mrgadget4711
 * ADD: Statistic capabilities
 *
 * ------------END------------
 */