package org.vqwiki;

import org.apache.log4j.Logger;

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

public class SearchRefreshThread extends Thread {

	private static SearchRefreshThread instance = null;
	private boolean refreshNow = false;
	private boolean endThread = false;
	int millis;
	private static final Logger logger = Logger.getLogger(SearchRefreshThread.class);

	/**
	 *
	 */
	public SearchRefreshThread(int interval) {
		instance = this;
		if (interval <= 0) {
			logger.debug("Inappropriate refresh interval: setting to 10");
			interval = 10;
		}
		this.millis = interval * 60 * 1000;
		start();
	}

	/**
	 *
	 */
	public void run() {
		while (!endThread) {
			try {
				sleep(this.millis);
			} catch (InterruptedException e) {
				logger.warn(e);
			}
			try {
				WikiBase.getInstance().getSearchEngineInstance().refreshIndex();
				refreshNow = false;
			} catch (java.io.IOException err) {
				logger.error(err);
			} catch (Exception err) {
				err.printStackTrace();
				logger.error(err);
			}
		}
	}

	/**
	 *
	 */
	private static SearchRefreshThread getInstance() {
		return instance;
	}

	/**
	 *
	 */
	public static void refreshNow() {
		if (getInstance() == null) return;
		getInstance().refreshNow = true;
		getInstance().interrupt();
	}

	/**
	 *
	 */
	public static void endThread() {
		if (getInstance() == null) return;
		getInstance().endThread = true;
		getInstance().interrupt();
	}
}
