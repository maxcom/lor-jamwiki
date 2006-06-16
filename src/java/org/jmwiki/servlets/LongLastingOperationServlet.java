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
package org.jmwiki.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmwiki.WikiException;
import org.jmwiki.utils.LongLastingOperationsManager;

/**
 * Abstract class to support long lasting operations
 *
 * This class was created on 00:24:36 23.07.2003
 *
 * @author $Author: wrh2 $
 */
public abstract class LongLastingOperationServlet extends VQWikiServlet implements Runnable {

	/** time, when everything starts */
	protected Date startingTime = null;
	/** Maximum time in seconds to wait until the page is refreshed */
	protected int MAX_TIME_TO_REFRESH = 3;
	/** progress done */
	public static final int PROGRESS_DONE = 1000;
	/** the url, which is used to call this servlet */
	protected String url;
	/** progress made */
	protected int progress = 0;
	/** The request, which is used during the operation */
	protected Locale locale = null;

	/**
	 * Handle post request.
	 * Generate a long lasting operation and execute it.
	 *
	 * @param request  The current http request
	 * @param response What the servlet will send back as response
	 *
	 * @throws ServletException If something goes wrong during servlet execution
	 * @throws IOException If the output stream cannot be accessed
	 *
	 */
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse response)
		throws ServletException, IOException {
		if (httpRequest.getParameter("norefresh") != null) {
			startingTime = new Date();
			locale = httpRequest.getLocale();
			url = httpRequest.getRequestURL().toString() + "?" + httpRequest.getQueryString();
			// execute task not in a thread
			run();
			dispatchDone(httpRequest, response);
		} else {
			LongLastingOperationsManager mgr = LongLastingOperationsManager.getInstance();
			int id;
			// check, if we are coming here again
			if (httpRequest.getParameter("id") == null) {
				// first time visitor: create new thread
				startingTime = new Date();
				locale = httpRequest.getLocale();
				url = httpRequest.getRequestURL().toString() + "?" + httpRequest.getQueryString();
				Thread t = new Thread(this);
				id = mgr.registerNewThread(this);
				t.setPriority(Thread.MIN_PRIORITY);
				t.start();
			} else {
				try {
					id = Integer.parseInt((String) httpRequest.getParameter("id"));
				} catch (NumberFormatException e) {
					id = 0;
				}
			}
			// get information on the thread
			httpRequest.setAttribute("id", new Integer(id));
			Runnable myThread = mgr.getThreadForId(id);
			if (myThread != null) {
				LongLastingOperationServlet myServlet = (LongLastingOperationServlet) myThread;
				httpRequest.setAttribute("progress", new Integer(myServlet.getProgress()));
				httpRequest.setAttribute("nextRefresh", new Integer(myServlet.getNextRefresh()));
				httpRequest.setAttribute("url", myServlet.getUrl());
				if (myServlet.getProgress() >= 100) {
					myServlet.dispatchDone(httpRequest, response);
					mgr.removeThreadById(id);
					return;
				}
			} else {
				error(httpRequest, response, new WikiException("Operation lost in /dev/null"));
				return;
			}
			dispatch("/WEB-INF/jsp/longlastingoperation.jsp", httpRequest, response);
		}
	}

	/**
	 * Handle get request.
	 * The request is handled the same way as the post request.
	 *
	 * @see doPost()
	 *
	 * @param httpServletRequest  The current http request
	 * @param httpServletResponse What the servlet will send back as response
	 *
	 * @throws ServletException If something goes wrong during servlet execution
	 * @throws IOException If the output stream cannot be accessed
	 *
	 */
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws ServletException, IOException {
		this.doPost(httpServletRequest, httpServletResponse);
	}

	/**
	 * What is the progress of the ongoing operation?
	 * @return int giving the percent (0 - 100), how much is finished.
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * How many seconds shall we wait until the next refresh?
	 * @return int giving the number of seconds to wait for the next refresh
	 */
	protected int getNextRefresh() {
		if (getProgress() < 50) {
			return MAX_TIME_TO_REFRESH;
		} else {
			long alreadyWorking = new Date().getTime() - startingTime.getTime();
			int timeToFinish = (int) ((double) alreadyWorking / ((double) getProgress() * 10.0));
			if (timeToFinish > MAX_TIME_TO_REFRESH) {
				return MAX_TIME_TO_REFRESH;
			} else {
				return timeToFinish;
			}
		}
	}

	/**
	 * Go to the done page
	 * @param request The current servlet request
	 * @param response The current servlet response
	 */
	protected abstract void dispatchDone(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

	/**
	 * Get the url, how this servlet is called
	 * @return
	 */
	protected String getUrl() {
		return url;
	}
}

/*
 * Log:
 *
 * $Log$
 * Revision 1.5  2006/04/23 06:36:55  wrh2
 * Coding style updates (VQW-73).
 *
 * Revision 1.4  2003/10/05 05:07:32  garethc
 * fixes and admin file encoding option + merge with contributions
 *
 * Revision 1.3  2003/08/20 20:41:41  mrgadget4711
 * ADD: Override refresh with norefresh=true
 *
 * Revision 1.2  2003/07/23 09:50:50  mrgadget4711
 * Fixes
 *
 * Revision 1.1  2003/07/23 00:34:26  mrgadget4711
 * ADD: Long lasting operations
 *
 * ------------END------------
 */