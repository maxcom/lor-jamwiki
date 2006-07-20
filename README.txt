INTRODUCTION
============

Please see http://jamwiki.org for the latest notes and documentation.  This file attempts to provide basic information for getting an instance of JAMWiki running on your web application server.  In addition, instructions are provided for those interested in building JAMWiki from source.


SUPPORTED CONFIGURATIONS
========================

JAMWiki requires a web application server (such as Tomcat or Websphere) that supports the following specifications:

  JDK 1.4 or later
  Servlet 2.3 or later

In addition, JAMWiki can be run in either a file persistency mode, or a database persistency mode.  When running in a file persistency mode a directory must be available into which JAMWiki files can be written.  When running in database persistency mode JAMWiki requires a database user with permission to create tables and sequences.  JAMWiki has been tested with the following databases:

  Postgres 8.0
  Postgres 7.4
  MySQL 4.1
  Oracle 10.2 (requires the 10g or later JDBC drivers)
  H2 Database (ANSI mode)

Note that JAMWiki may work with any ANSI compliant database.  Also note that to support double-byte charaters the database should use UTF-8 encoding.


INSTALLATION
============

JAMWiki is distributed as a WAR file that can be installed onto any web application server that supports the JDK 1.4 and servlet 2.3 specifications.  Refer to the web application server documentation for specific instructions on how to deploy a WAR file.  Once the WAR file has been installed, restart the web application server and view the page http://<server>/<context>/ to begin the configuration process (<server> is the URL for your server, and <context> is the web application server context under which JAMWiki was installed).

The setup process begins with the first JAMWiki pageview after setup.  Setup requires the following:

  1. A directory into which JAMWiki files can be written.
  2. A directory (accessible to the web server) into while images and other
     files can be uploaded.
  3. The relative path (with respect to the web server doc root) to the image
     upload directory.
  4. The name and login of an administrative user.
  5. (Optional) If using a database for persistency then the database settings
     must also be provided (see the "Database Settings" section below).

Once the settings have been verified JAMWiki will create the user account, database tables or file directories, base properties, and default topics.  Once complete JAMWiki redirects to the starting page, ready for use.  That's all there is to it!


UPGRADES
========

The process for upgrading JAMWiki is:

  1. Download the latest JAMWiki WAR file.
  2. Back up all database and/or file data prior to upgrading.
  3. Back up the jamwiki.properties file and the log4j.properties file that can
     be found in the /WEB-INF/classes directory.
  4. If you have created any virtual wikis, back up the web.xml file that can be
     found in the /WEB-INF directory.
  5. Install the new JAMWiki WAR file.  See your web application server's
     documentation for instructions.
  6. Copy the files backed up in steps three and four back into their old
     locations, overwriting any new files.
  7. View any page on the Wiki.  You will be redirected to the upgrade page
     and any required upgrade steps will be automatically performed.


DATABASE SETTINGS
=================

JAMWiki can operate using files for storage, or using a database.  For larger implementations a database is highly recommended.  To utilize a database the following steps are required during initial setup:

  1. Install an appropriate JDBC driver in the WEB-INF/lib directory.  JDBC
     driver packages can normally be obtained from the database vendor.
  2. Create the JAMWiki database.  JAMWiki can also use an existing database.
  3. Create a user login for JAMWiki.  The user must have permission to create
     tables and sequences.
  4. During setup choose "Database" from the persistency-type dropdown menu.  For
     Oracle, MySql and Postgres choose the appropriate database type; for all
     other database types choose Ansi.  Fill out the driver, url, username
     and password fields.  Check with your database vendor to determine the
     appropriate values for each of these fields.  Some example values are below:

       JDBC driver class: org.postgresql.Driver
       JDBC driver class: com.mysql.jdbc.Driver
       JDBC driver class: oracle.jdbc.driver.OracleDriver
       Database type    : as appropriate
       Database URL     : jdbc:postgresql://localhost:5432/<database_name>
       Database URL     : jdbc:mysql://localhost:3306/<database_name>
       Database URL     : jdbc:oracle:thin:@//localhost:1521/<service_name>
       Database Username: as appropriate
       Database Password: as appropriate

  5. JAMWiki will verify that a connection can be established with the
     database and will then create all required tables.


VIRTUAL WIKIS
=============

JAMWiki provides the capability to create "virtual wikis", which are distinct wikis running under the same web application.  By default, a virtual wiki named "en" is created during installation.  The default URL for files within this virtual wiki is then of the form "http://<server>/<context>/en/Topic".  To create a new virtual wiki the following steps are required:

1. Access the admin console at http://<server>/<context>/en/Special:Admin.
2. Scroll down to the "add virtual wiki" box, enter a name (one word, no spaces) and click add.
3. Shut down the web application server.
4. Edit the web application web.xml file.  There will be a mapping of the form:

    <servlet-mapping>
        <servlet-name>jamwiki</servlet-name>
        <url-pattern>/en/*</url-pattern>
    </servlet-mapping>

5. Enter a new servlet-mapping, replacing "en" in the above example with the name of the new virtual wiki.
6. Restart the web application server.
7. A new virtual wiki will now be available from URLs of the form "http://<server>/<context>/<virtual-wiki>/Topic.


BUILDING FROM SOURCE
====================

The JAMWiki source is available from the Subversion source repository on Sourceforge.net.  To check out the code, first install Subversion and then execute the following command:

svn co https://svn.sourceforge.net/svnroot/jamwiki/trunk jamwiki

This command will copy the current development code (the "trunk") into a local directory named "jamwiki".

The software can be built from the ANT build script provided.  To build the software, install ANT (http://ant.apache.org/) and a JDK version 1.4 or later.  Once ANT and the JDK are properly installed, run the following commands:

cd trunk
ant war

The software should build, and when complete a jamwiki-x.x.x.war file will be located in the trunk/build directory.  Consult your web application server's documentation for instructions on how to install this file.


SOURCE REPOSITORY LAYOUT
========================

The source code repository is organized as follows:

jamwiki (the root)
   |- lib
   |- src
   |   |- java
   |   |    |- org
   |   |        |- jamwiki
   |   |- lex
   |   |    |- alt
   |   |- lib
   |   |- resources
   |   |- webapp
   |        |- WEB-INF
   |             |- jsp
   |- build (generated during build)


* jamwiki (root)

This directory contains the Ant build files and this file.

* jamwiki/lib

This directory contains all the libraries needed for JAMWiki.

* jamwiki/src/java

Contains all java files belonging to the project. The project files are tucked into a package structure org.jamwiki.*
    
* jamwiki/src/lex

Contains the lexer source files from which the lexer (for example JFlex) will generate the java source code for later use in compilation and building. The lex directory only contains the core lexer. The "alt" subdirectory contains any alternative lexers distributed with the project.

* jamwiki/src/lib

Contains all the libraries that should be packaged into the final WAR file. These libraries CAN be used in the build process.

* jamwiki/src/webapp

All the web application files are located in here conforming to the Maven idea.
