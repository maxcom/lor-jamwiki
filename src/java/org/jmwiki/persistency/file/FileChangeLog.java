/**
 *
 */
package org.jmwiki.persistency.file;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import JSX.ObjIn;
import JSX.ObjOut;
import org.apache.log4j.Logger;
import org.jmwiki.*;
import org.jmwiki.utils.JSPUtils;
import org.jmwiki.utils.Utilities;

/**
 *
 */
public class FileChangeLog implements ChangeLog {

	private static final Logger logger = Logger.getLogger(FileChangeLog.class);
	private static FileChangeLog instance;
	// recent changes file
	public static final String RECENTFILE = "recent.xml";
	private HashMap cache;

	/**
	 *
	 */
	private FileChangeLog() {
		cache = new HashMap();
	}

	/**
	 *
	 */
	private Hashtable getTableFor(String virtualWiki) throws IOException, ClassNotFoundException {
		if (virtualWiki == null) virtualWiki = "";
		if (cache.containsKey(virtualWiki)) {
			return (Hashtable) cache.get(virtualWiki);
		}
		File changesPath = FileHandler.getPathFor(virtualWiki, RECENTFILE);
		logger.debug("reading changes for " + virtualWiki + " from " + changesPath);
		Hashtable vwikiCache;
		if (!changesPath.exists()) {
			logger.debug("no file exists, creating now");
			vwikiCache = new Hashtable();
			cache.put(virtualWiki, vwikiCache);
			saveTableFor(virtualWiki);
			return vwikiCache;
		}
		logger.debug("reading file from disk");
		FileInputStream fileInputStream = new FileInputStream(changesPath);
		ObjIn in = new ObjIn(fileInputStream);
		vwikiCache = (Hashtable) in.readObject();
		cache.put(virtualWiki, vwikiCache);
		in.close();
		fileInputStream.close();
		return vwikiCache;
	}

	/**
	 *
	 */
	private void saveTableFor(String virtualWiki) throws IOException {
		if (virtualWiki == null) virtualWiki = "";
		File changesPath = FileHandler.getPathFor(virtualWiki, RECENTFILE);
		FileOutputStream fileOutputStream = new FileOutputStream(changesPath);
		ObjOut out = new ObjOut(fileOutputStream);
		Object changeTable = cache.get(virtualWiki);
		logger.debug("Change table for " + virtualWiki + ": " + changeTable);
		out.writeObject(changeTable);
		out.close();
		fileOutputStream.close();
		logger.debug("wrote changes to disk: " + changesPath);
	}

	/**
	 *
	 */
	public static final FileChangeLog getInstance() {
		if (instance == null) instance = new FileChangeLog();
		return instance;
	}

	/**
	 *
	 */
	public synchronized void logChange(Change change, HttpServletRequest request)
		throws IOException, FileNotFoundException, ClassNotFoundException {
		logger.debug("logging change " + change);
		String date = Utilities.formatDate(new Date());
		String virtualWiki = change.getVirtualWiki();
		Hashtable changesTable = getTableFor(virtualWiki);
		Vector changesInFile;
		if (changesTable.get(date) == null) {
			logger.debug("no changes on " + date);
			changesInFile = new Vector();
			changesTable.put(date, changesInFile);
		} else {
			logger.debug("adding to changes for " + date);
			changesInFile = (Vector) changesTable.get(date);
		}
		boolean suppressNotifyInSameDay =
			Environment.getBooleanValue(Environment.PROP_EMAIL_SUPPRESS_NOTIFY_WITHIN_SAME_DAY);
		logger.debug("changesInFile: " + changesInFile);
		boolean changedToday = false;
		if (changesInFile.contains(change)) {
			if (logger.isDebugEnabled()) {
				logger.debug("removing existing change");
				if (suppressNotifyInSameDay) {
					logger.debug("not sending notifications because the topic has already been changed today");
				}
			}
			changesInFile.remove(change);
			changedToday = true;
		}
		if (!changedToday && suppressNotifyInSameDay || !suppressNotifyInSameDay) {
			try {
				logger.debug("running notifier");
				Notify notifier = new FileNotify(virtualWiki, change.getTopic());
				String wikiServerHostname = Environment.getValue(Environment.PROP_BASE_SERVER_HOSTNAME);
				notifier.sendNotifications(JSPUtils.createRootPath(request, virtualWiki, wikiServerHostname), request.getLocale());
			} catch (Exception e) {
				logger.warn("exception occurred in notifier", e);
				e.printStackTrace();
			}
		}
		// add change to front of Vector, so that date-sorting within day works
		// correctly.
		logger.debug("adding change " + change);
		changesInFile.add(0, change);
		saveTableFor(virtualWiki);
	}

	/**
	 * Get all recent changes for a particular date
	 *
	 * @param virtualWiki
	 *			the virtual wiki
	 * @param d
	 *			the date of the changes
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	public Collection getChanges(String virtualWiki, Date d) throws IOException, FileNotFoundException, ClassNotFoundException {
		String date = Utilities.formatDate(d);
		return (Collection) getTableFor(virtualWiki).get(date);
	}

	/**
	 * Delete the existing recent changes file
	 *
	 * @param virtualWiki
	 */
	public void deleteChangeTableFile(String virtualWiki) {
		try {
			FileHandler.getPathFor(virtualWiki, RECENTFILE).delete();
		} catch (Exception err) {
			logger.error("Unable to delete recent changes file for " + virtualWiki, err);
		}
	}

	/**
	 *
	 */
	public void removeChanges(String virtualwiki, Collection cl) throws IOException, ClassNotFoundException {
		Hashtable changesTable = getTableFor(virtualwiki);
		logger.debug("purging topics from the recent filelist");
		Set changesSet = changesTable.keySet();
		Set removesSet = new HashSet();
		for (Iterator iter = changesSet.iterator(); iter.hasNext();) {
			Object element = iter.next();
			Vector changes = (Vector) changesTable.get(element);
			Vector removeChanges = new Vector();
			for (Iterator iterator = changes.iterator(); iterator.hasNext();) {
				Change change = (Change) iterator.next();
				if (change.getVirtualWiki().equals(virtualwiki) && cl.contains(change.getTopic())) {
					logger.debug("remove (purge) topic from the changelog: " + change.getTopic());
					removeChanges.add(change);
				}
			}
			if (changes.size() == removeChanges.size()) {
				removesSet.add(element);
			} else {
				changes.removeAll(removeChanges);
			}
		}
		changesSet.removeAll(removesSet);
		saveTableFor(virtualwiki);
	}
}
