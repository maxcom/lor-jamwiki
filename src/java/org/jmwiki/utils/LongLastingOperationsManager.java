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
package org.jmwiki.utils;

import java.util.Iterator;
import java.util.Vector;

import org.jmwiki.servlets.LongLastingOperationServlet;

/**
 * Singleton class, which manages long lasting operations
 *
 * This class was created on 00:25:22 23.07.2003
 *
 * @author $Author: wrh2 $
 */
public class LongLastingOperationsManager {

	/** instance of this class */
	private static LongLastingOperationsManager instance = new LongLastingOperationsManager();

	/** a vector of all ongoing threads */
	Vector onGoingThreads = new Vector();

	/**
	 * Constructor; private. Use getInstance() instead
	 *
	 * @see getInstance()
	 */
	private LongLastingOperationsManager() {
	}

	/**
	 *
	 */
	public static LongLastingOperationsManager getInstance() {
		return instance;
	}

	/**
	 * Register a new thread to the manager
	 * @param t The thread to register
	 * @return the id of this thread
	 */
	public synchronized int registerNewThread(Runnable r) {
		// go through vector if threads already ended can be removed:
		boolean somethingremoved = false;
		do {
			somethingremoved = false;
			for (Iterator iter = onGoingThreads.iterator(); !somethingremoved && iter.hasNext();) {
				LongLastingOperationServlet aThread = (LongLastingOperationServlet) iter.next();
				if (aThread.getProgress() >= LongLastingOperationServlet.PROGRESS_DONE) {
					iter.remove();
					somethingremoved = true;
				}
			}
		}
		while (somethingremoved);
		// register new thread
		int id = onGoingThreads.size();
		onGoingThreads.add(id, r);
		return id;
	}

	/**
	 * Get a thread for a certain identifier
	 * @param string The identifier of this thred
	 * @return the thread or null, if the thread cannot be found
	 */
	public Runnable getThreadForId(String idStr) {
		try {
			int id = Integer.parseInt(idStr);
			return (Runnable) onGoingThreads.get(id);
		} catch (Exception e) {
			// in case of error:
			return null;
		}
	}

	/**
	 * Get a thread for a certain identifier
	 * @param int The identifier of this thred
	 * @return the thread or null, if the thread cannot be found
	 */
	public Runnable getThreadForId(int id) {
		try {
			return (Runnable) onGoingThreads.get(id);
		} catch (Exception e) {
			// in case of error:
			return null;
		}
	}

	/**
	 * Remove a thread by its identifier
	 * @param id The id if the thread to be removed
	 */
	public void removeThreadById(int id) {
		try {
			onGoingThreads.remove(id);
		} catch (Exception e) {
			;
		}
	}
}

/*
 * Log:
 *
 * $Log$
 * Revision 1.4  2006/04/23 04:45:45  wrh2
 * Coding style updates (VQW-73).
 *
 * Revision 1.3  2003/10/05 05:07:32  garethc
 * fixes and admin file encoding option + merge with contributions
 *
 * Revision 1.2  2003/07/23 09:50:54  mrgadget4711
 * Fixes
 *
 * Revision 1.1  2003/07/23 00:34:26  mrgadget4711
 * ADD: Long lasting operations
 *
 * ------------END------------
 */