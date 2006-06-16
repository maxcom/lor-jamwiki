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
public class StatisticsOneWikiBean {

	private String name = null;
	private String numpages = null;
	private String numchanges = null;
	private String nummodifications = null;
	private String numpageslw = null;
	private String numchangeslw = null;
	private String ratiolw = null;
	private List months = new ArrayList();
	private List authors = new ArrayList();

	/**
	 * @return
	 */
	public List getAuthors() {
		return authors;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getNumauthors() {
		if (authors == null) return "0";
		return String.valueOf(authors.size());
	}

	/**
	 * @return
	 */
	public String getNumchanges() {
		return numchanges;
	}

	/**
	 * @return
	 */
	public String getNumchangeslw() {
		return numchangeslw;
	}

	/**
	 * @return
	 */
	public String getNummodifications() {
		return nummodifications;
	}

	/**
	 * @return
	 */
	public String getNumpages() {
		return numpages;
	}

	/**
	 * @return
	 */
	public String getNumpageslw() {
		return numpageslw;
	}

	/**
	 * @return
	 */
	public String getRatiolw() {
		return ratiolw;
	}

	/**
	 * @param list
	 */
	public void setAuthors(List list) {
		authors = list;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setNumchanges(String string) {
		numchanges = string;
	}

	/**
	 * @param string
	 */
	public void setNumchangeslw(String string) {
		numchangeslw = string;
	}

	/**
	 * @param string
	 */
	public void setNummodifications(String string) {
		nummodifications = string;
	}

	/**
	 * @param string
	 */
	public void setNumpages(String string) {
		numpages = string;
	}

	/**
	 * @param string
	 */
	public void setNumpageslw(String string) {
		numpageslw = string;
	}

	/**
	 * @param string
	 */
	public void setRatiolw(String string) {
		ratiolw = string;
	}

	/**
	 * @return
	 */
	public List getMonths() {
		return months;
	}

	/**
	 * @param list
	 */
	public void setMonths(List list) {
		months = list;
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