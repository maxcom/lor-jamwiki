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