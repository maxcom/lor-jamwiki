/**
 *
 */
package org.jmwiki.persistency.db;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.AbandonedObjectPool;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.utils.Encryption;

/**
 *
 */
public class DatabaseConnection {

	private static final Logger logger = Logger.getLogger(DatabaseConnection.class);
	private static boolean poolInitialized = false;

	/**
	 *
	 */
	public static void setUpConnectionPool(String url, String userName, String password) throws Exception {
		Class.forName(Environment.getValue(Environment.PROP_DB_DRIVER));
		AbandonedConfig config = new AbandonedConfig();
		config.setRemoveAbandoned(Environment.getBooleanValue(Environment.PROP_DBCP_REMOVE_ABANDONED));
		config.setLogAbandoned(Environment.getBooleanValue(Environment.PROP_DBCP_LOG_ABANDONED));
		config.setRemoveAbandonedTimeout(Environment.getIntValue(Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT));
		GenericObjectPool connectionPool = new AbandonedObjectPool(null, config);
		connectionPool.setMaxActive(Environment.getIntValue(Environment.PROP_DBCP_MAX_ACTIVE));
		connectionPool.setMaxIdle(Environment.getIntValue(Environment.PROP_DBCP_MAX_IDLE));
		connectionPool.setMinEvictableIdleTimeMillis(Environment.getIntValue(Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME) * 1000);
		connectionPool.setTestOnBorrow(Environment.getBooleanValue(Environment.PROP_DBCP_TEST_ON_BORROW));
		connectionPool.setTestOnReturn(Environment.getBooleanValue(Environment.PROP_DBCP_TEST_ON_RETURN));
		connectionPool.setTestWhileIdle(Environment.getBooleanValue(Environment.PROP_DBCP_TEST_WHILE_IDLE));
		connectionPool.setTimeBetweenEvictionRunsMillis(Environment.getIntValue(Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS) * 1000);
		connectionPool.setNumTestsPerEvictionRun(Environment.getIntValue(Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN));
		connectionPool.setWhenExhaustedAction((byte) Environment.getIntValue(Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION));
		DriverManagerConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, userName, password);
		new PoolableConnectionFactory(connectionFactory, connectionPool, null, Environment.getValue(Environment.PROP_DBCP_VALIDATION_QUERY), false, true);
		PoolingDriver driver = new PoolingDriver();
		driver.registerPool("jmwiki", connectionPool);
		poolInitialized = true;
	}

	/**
	 *
	 */
	public static Connection getConnection() throws Exception {
		String url = Environment.getValue(Environment.PROP_DB_URL);
		String userName = Environment.getValue(Environment.PROP_DB_USERNAME);
		String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD);
		Connection conn = null;
		if (url.startsWith("jdbc:")) {
			if (!poolInitialized) {
				setUpConnectionPool(url, userName, password);
			}
			conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:jmwiki");
		} else {
			// Use Reflection here to avoid a compile time dependency
			// on the DataSource interface. It's not available by default
			// on j2se 1.3.
			Context ctx = new InitialContext();
			Object dataSource = ctx.lookup(url);
			Method m;
			Object args[];
			if (userName.length() == 0) {
				Class[] parameterTypes = null;
				m = dataSource.getClass().getMethod("getConnection", parameterTypes);
				args = new Object[]{};
			} else {
				m = dataSource.getClass().getMethod("getConnection", new Class[]{String.class, String.class});
				args = new Object[]{userName, password};
			}
			conn = (Connection)m.invoke(dataSource, args);
		}
		conn.setAutoCommit(true);
		return conn;
	}

	/**
	 *
	 */
	public static void setPoolInitialized(boolean poolInitialized) {
		DatabaseConnection.poolInitialized = poolInitialized;
	}

	/**
	 * Utility method for closing a database connection, a statement and a result set.
	 * This method must ALWAYS be called for any connection retrieved by the
	 * {@link DatabaseConnection#getConnection getConnection()} method, and the
	 * connection SHOULD NOT have already been closed.
	 *
	 * @param conn A database connection, retrieved using DatabaseConnection.getConnection(),
	 *  that is to be closed.  This connection SHOULD NOT have been previously closed.
	 * @param stmt A statement object that is to be closed.  May be <code>null</code>.
	 * @param rs A result set object that is to be closed.  May be <code>null</code>.
	 */
	public static void closeConnection(Connection conn, Statement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {}
		}
		DatabaseConnection.closeConnection(conn, stmt);
	}

	/**
	 * Utility method for closing a database connection and a statement.  This method
	 * must ALWAYS be called for any connection retrieved by the
	 * {@link DatabaseConnection#getConnection getConnection()} method, and the
	 * connection SHOULD NOT have already been closed.
	 *
	 * @param conn A database connection, retrieved using DatabaseConnection.getConnection(),
	 *  that is to be closed.  This connection SHOULD NOT have been previously closed.
	 * @param stmt A statement object that is to be closed.  May be <code>null</code>.
	 */
	public static void closeConnection(Connection conn, Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {}
		}
		DatabaseConnection.closeConnection(conn);
	}

	/**
	 * Utility method for closing a database connection.  This method must ALWAYS be
	 * called for any connection retrieved by the
	 * {@link DatabaseConnection#getConnection getConnection()} method, and the
	 * connection SHOULD NOT have already been closed.
	 *
	 * @param conn A database connection, retrieved using DatabaseConnection.getConnection(),
	 *  that is to be closed.  This connection SHOULD NOT have been previously closed.
	 */
	public static void closeConnection(Connection conn) {
		if (conn == null) {
			logger.info("Attempt to call DatabaseConnection.closeConnection() with null connection value");
			return;
		}
		try {
			conn.close();
		} catch (Exception e) {
			logger.error("Failure while closing connection", e);
		}
	}
}
