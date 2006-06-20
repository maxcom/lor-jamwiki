/**
 *
 */
package org.jmwiki;

import org.apache.log4j.Logger;

/**
 *
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
