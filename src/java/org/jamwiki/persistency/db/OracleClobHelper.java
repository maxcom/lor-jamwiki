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
package org.jmwiki.persistency.db;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.SQLException;

/**
 *
 */
public class OracleClobHelper {

	/**
	 *
	 */
	public static String getClobValue(Clob clob) throws SQLException, IOException {
		if (clob == null) return "";
		StringBuffer sb = new StringBuffer();
		char buffer[] = new char[4096];
		Reader r = clob.getCharacterStream();
		while (true) {
			int n = r.read(buffer);
			if (n == -1) break;
			sb.append(buffer, 0, n);
		}
		r.close();
		return new String(sb);
	}

	/**
	 *
	 */
	public static void setClobValue(Clob clob, String value) throws SQLException,
		IOException, ClassNotFoundException, SecurityException, NoSuchMethodException,
		IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// Use Reflection here to avoid a compile time dependency
		// on the Oracle JDBC driver.
		Class oracleClobClass = Class.forName("oracle.sql.CLOB");
		Method m = oracleClobClass.getMethod("getCharacterOutputStream", new Class[]{});
		Writer w = (Writer) m.invoke(clob, new Object[]{});
		w.write(value);
		w.close();
	}
}
