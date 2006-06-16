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
package org.jmwiki.persistency.db;

import org.apache.log4j.Logger;
import org.jmwiki.Change;
import org.jmwiki.ChangeLog;
import org.jmwiki.Notify;
import org.jmwiki.WikiBase;
import org.jmwiki.Environment;
import org.jmwiki.utils.JSPUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class DatabaseChangeLog implements ChangeLog {

	private static final Logger logger = Logger.getLogger(DatabaseChangeLog.class);
	protected static DatabaseChangeLog instance;

	protected final static String STATEMENT_WRITE_CHANGE =
		"INSERT INTO TopicChange( topic, username, changeat, virtualwiki ) VALUES( ?, ?, ?, ? )";
	protected final static String STATEMENT_READ_CHANGES =
		"SELECT * FROM TopicChange WHERE changeat >= ? and changeat <= ? and virtualwiki = ? ORDER BY changeat DESC";
	protected final static String STATEMENT_CHANGE_EXISTS =
		"SELECT * FROM TopicChange WHERE topic = ? AND changeat >= ? and changeat <= ? and virtualwiki = ?";
	protected final static String STATEMENT_UPDATE_CHANGE =
		"UPDATE TopicChange SET username = ?, changeat = ? WHERE topic = ? AND changeat = ? and virtualwiki = ?";
	protected final static String STATEMENT_DELETE_CHANGE =
		"DELETE FROM TopicChange WHERE topic = ? AND virtualwiki = ?";

	/**
	 *
	 */
	private DatabaseChangeLog() {
	}

	/**
	 *
	 */
	public static DatabaseChangeLog getInstance() {
		if (instance == null) instance = new DatabaseChangeLog();
		return instance;
	}

	/**
	 *
	 */
	public void logChange(Change change, HttpServletRequest request) throws Exception {
		logger.debug("Logging change: " + change);
		Connection conn = null;
		String virtualWiki = null;
		String topic = null;
		boolean changedToday = false;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement existChangeStatement = conn.prepareStatement(STATEMENT_CHANGE_EXISTS);
			topic = change.getTopic();
			existChangeStatement.setString(1, topic);
			existChangeStatement.setTimestamp(2, (new DBDate(change.getTime())).startOfDayStamp());
			existChangeStatement.setTimestamp(3, (new DBDate(change.getTime())).endOfDayStamp());
			virtualWiki = change.getVirtualWiki();
			existChangeStatement.setString(4, virtualWiki);
			ResultSet rs = existChangeStatement.executeQuery();
			if (rs.next()) {
				changedToday = true;
				logger.debug("Updating as existing change for " + change.getUser());
				DBDate changeDate = new DBDate(rs.getTimestamp("changeat"));
				rs.close();
				PreparedStatement updateChangeStatement = conn.prepareStatement(STATEMENT_UPDATE_CHANGE);
				updateChangeStatement.setString(1, change.getUser());
				updateChangeStatement.setTimestamp(2, (new DBDate()).asTimestamp());
				updateChangeStatement.setString(3, topic);
				updateChangeStatement.setTimestamp(4, changeDate.asTimestamp());
				updateChangeStatement.setString(5, virtualWiki);
				updateChangeStatement.execute();
				updateChangeStatement.close();
			} else {
				logger.debug("New change for " + change.getUser());
				rs.close();
				PreparedStatement writeChangeStatement = conn.prepareStatement(STATEMENT_WRITE_CHANGE);
				writeChangeStatement.setString(1, topic);
				writeChangeStatement.setString(2, change.getUser());
				writeChangeStatement.setTimestamp(3, (new DBDate()).asTimestamp());
				writeChangeStatement.setString(4, virtualWiki);
				writeChangeStatement.execute();
				writeChangeStatement.close();
			}
			rs.close();
			existChangeStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		boolean suppressNotifyInSameDay =
			Environment.getBooleanValue(Environment.PROP_EMAIL_SUPPRESS_NOTIFY_WITHIN_SAME_DAY);
		if (!changedToday && suppressNotifyInSameDay || !suppressNotifyInSameDay) {
			try {
				Notify notifier = WikiBase.getInstance().getNotifyInstance(virtualWiki, topic);
				String wikiServerHostname = Environment.getValue(Environment.PROP_BASE_SERVER_HOSTNAME);
				notifier.sendNotifications(JSPUtils.createRootPath(request, virtualWiki, wikiServerHostname), request.getLocale());
			} catch (Exception e) {
				logger.warn(e);
				e.printStackTrace();
			}
		} else {
			logger.debug("not sending notification because change has already been made today");
		}
	}

	/**
	 *
	 */
	public Collection getChanges(String virtualWiki, Date d) throws Exception {
		logger.debug("Getting changes for virtualWiki " + virtualWiki);
		Collection all = new ArrayList();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement readChangesStatement = conn.prepareStatement(STATEMENT_READ_CHANGES);
			readChangesStatement.setTimestamp(1, (new DBDate(d)).startOfDayStamp());
			readChangesStatement.setTimestamp(2, (new DBDate(d)).endOfDayStamp());
			readChangesStatement.setString(3, virtualWiki);
			ResultSet rs = readChangesStatement.executeQuery();
			while (rs.next()) {
				Change change = new Change();
				change.setUser(rs.getString("username"));
				change.setTopic(rs.getString("topic"));
				change.setTime(new DBDate(rs.getTimestamp("changeat")));
				change.setVirtualWiki(virtualWiki);
				all.add(change);
			}
			rs.close();
			readChangesStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return all;
	}

	/**
	 *
	 */
	public void removeChanges(String virtualwiki, Collection cl) throws Exception {
		logger.debug("purging topics from the recent topics list for virtualWiki " + virtualwiki);
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			PreparedStatement deleteChangesStatement = conn.prepareStatement(STATEMENT_DELETE_CHANGE);
			for (Iterator iter = cl.iterator(); iter.hasNext();) {
				String topic = (String) iter.next();
				logger.debug("add topic to delete " + topic);
				deleteChangesStatement.setString(1, topic);
				deleteChangesStatement.setString(2, virtualwiki);
				deleteChangesStatement.execute();
			}
			deleteChangesStatement.close();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}
}
