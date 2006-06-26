/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jmwiki;

import java.util.Calendar;
import java.util.Date;

/*
 *
 */
public class Change implements java.io.Serializable, Comparable {

	protected String topic;
	protected String user;
	protected Date time;
	protected String virtualWiki;

	/**
	 *
	 */
	public Change() {
	}

	/**
	 *
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 *
	 */
	public Change(String virtualWiki, String inTopic, String inUser, Date changeDate) {
		this.topic = inTopic;
		this.user = inUser;
		this.time = changeDate;
		this.virtualWiki = virtualWiki;
	}

	/**
	 *
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 *
	 */
	public String getUser() {
		return user;
	}

	/**
	 *
	 */
	public String getUsername() {
		try {
			return WikiBase.getInstance().getUsergroupInstance().getFullnameById(user);
		} catch (Exception e) {
			return user;
		}
	}

	/**
	 *
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 *
	 */
	public Date getTime() {
		return time;
	}

	/**
	 *
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 *
	 */
	public String getVirtualWiki() {
		return virtualWiki;
	}

	/**
	 *
	 */
	public void setVirtualWiki(String virtualWiki) {
		this.virtualWiki = virtualWiki;
	}

	/**
	 *
	 */
	public int compareTo(Object o) {
		Change incomingChange = (Change) o;
		if (time.before(incomingChange.time)) {
			return 1;
		}
		if (time.after(incomingChange.time)) {
			return -1;
		}
		return 0;
	}

	/**
	 *
	 */
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof Change)) return false;
		Change c = (Change) o;
		if (!this.topic.equals(c.getTopic()) || !this.user.equals(c.getUser())) {
			return false;
		}
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(this.time);
		cal2.setTime(c.getTime());
		if (cal1.get(Calendar.DATE) != cal2.get(Calendar.DATE) ||
			cal1.get(Calendar.MONTH) != cal2.get(Calendar.MONTH) ||
			cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) {
			return false;
		}
		return true;
	}
}
