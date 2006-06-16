/*
Java MediaWiki - WikiWikiWeb clone
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

import java.util.ArrayList;
import java.util.List;


/**
 * Bean containing results for a statistic page
 *
 * This class was created on 09:34:30 19.07.2003
 *
 * @author $Author: wrh2 $
 */
public class StatisticsVWikiBean {

	private List vwiki = new ArrayList();

	/**
	 * @return
	 */
	public String getNumwikis() {
		if (vwiki == null) return "0";
		return String.valueOf(vwiki.size());
	}

	/**
	 * @return
	 */
	public String getShowwikis() {
		if (vwiki == null) return "true";
		if (vwiki.size() == 1) return "false";
		return "true";
	}

	/**
	 * @return
	 */
	public List getVwiki() {
		return vwiki;
	}

	/**
	 * @param list
	 */
	public void setVwiki(List list) {
		vwiki = list;
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