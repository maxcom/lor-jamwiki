INTRODUCTION
============

This file attempts to aid you in coming to understand the structure of the JAMWiki project and to tell you what you need to know in order to be able to build a working, installable WAR file from the source code.


PREREQUISITES
=============

JAMWiki is built to the following specifications:

  JDK 1.4 or later
  Servlet 2.3 or later
  Postgres 8.x (may work on earlier or later versions, untested)
  Tomcat 5.x (should work on other app servers, untested)


BUILDING THE APPLICATION
========================

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

This directory contains all the libraries needed for the BUILD PROCESS ONLY. The libraries in this directory are NOT packaged into the final WAR file. This allows us to focus on building JAMWiki for a certain Java release.

* jamwiki/src/java

Contains all java files belonging to the project. The project files are tucked into a package structure org.jamwiki.*
    
* jamwiki/src/lex

Contains the lexer source files from which the lexer (for example JFlex) will generate the java source code for later use in compilation and building. The lex directory only contains the core lexer. The "alt" subdirectory contains any alternative lexers distributed with the project.

* jamwiki/src/lib

Contains all the libraries that should be packaged into the final WAR file. These libraries CAN be used in the build process.

* jamwiki/src/webapp

All the web application files are located in here conforming to the Maven idea.


DATABASE SETTINGS
=================

JAMWiki can operate using files for storage, or using a database.  For larger implementations a database is highly recommended.  To utilize a database the following steps are required during initial setup:

1. Install an appropriate JDBC driver in the WEB-INF/lib directory.  JDBC driver packages can normally be obtained from the database vendor.

2. Create the JAMWiki database.  JAMWiki can also use an existing database.

3. During setup choose "Database" from the persistency-type dropdown menu and fill out the driver, database type, url, username and password fields.  Check with your database vendor to determine the appropriate values for each of these fields.  Some example values are below:

    JDBC driver class: org.postgresql.Driver
    Database type    : postgres
    Database URL     : jdbc:postgresql://localhost:5432/jamwiki
    Database Username: as appropriate
    Database Password: as appropriate

4. JAMWiki will verify that a connection can be established with the database and will then create all required tables.


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