/**
 *
 */
package org.jmwiki.persistency.db;

import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class DBDate extends java.util.Date {

	/**
	 *
	 */
	public DBDate(java.sql.Timestamp stamp) {
		super(stamp.getTime());
	}

	/**
	 *
	 */
	public DBDate(java.util.Date date) {
		this.setTime(date.getTime());
	}

	/**
	 *
	 */
	public DBDate() {
		super();
	}

	/**
	 *
	 */
	public boolean equals(Object o) {
		if (!(o instanceof java.util.Date)) return false;
		Date other = (Date) o;
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(this);
		cal2.setTime(other);
		cal1.set(Calendar.MILLISECOND, 0);
		cal2.set(Calendar.MILLISECOND, 0);
		return cal1.equals(cal2);
	}

	/**
	 *
	 */
	public java.sql.Timestamp asTimestamp() {
		return new java.sql.Timestamp(this.getTime());
	}

	/**
	 *
	 */
	public java.sql.Timestamp startOfDayStamp() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new java.sql.Timestamp(cal.getTime().getTime());
	}

	/**
	 *
	 */
	public java.sql.Timestamp endOfDayStamp() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return new java.sql.Timestamp(cal.getTime().getTime());
	}
}
