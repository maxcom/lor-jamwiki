
----------------------------------------------------------------------------
-- DataSource README for JAMWiki V2.5
----------------------------------------------------------------------------


JAMWiki can use a J2EE style DataSource retrieved through JNDI as a source of
database connections. The procedure is as follows:

Configure a JDBC DataSource in your container and make it available
through JNDI. Consult your container's documentation for details. Note the
JNDI name needed to look it up. JAMWiki will look up the name from an
InitialContext instance, so you'll probably have to prefix your JNDI name
with "java:comp/env/". For example: "java:comp/env/jdbc/JAMWikiDb".

In the administration servlet, configure JAMWiki for database
persistence as usual, but with the following changes:

- The driver class name field can be left empty.

- All connection pool settings are ignored.

- Either configure a username and password in your DataSource, or
specify a username/password pair in the admin servlet, but not
both. I've only tried the former, but the latter should work if your
DataSource and JDBC driver support it. Leave the username and password
fields empty if your DataSource has the login credentials.

- Set the Database URL to the JNDI name. JAMWiki distinguishes between
the two by looking at the URL. If it starts with "jdbc:", it's assumed
to be a JDBC URL, otherwise, it is assumed to be a JNDI name.


That's it. If you only want to use a DataSource to realize connection
pooling/testing/recycling, consider using the builtin connection pooling
support instead. Read CONNECTION_POOL for more details.

