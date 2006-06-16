- INTRODUCTION -
================
This file attempts to aid you in coming to understand the structure of the JMWiki project and to tell you what you need to know in order to be able to
build a working, installable WAR file from the source code.

Contents:
    1) Building the software
    2) Directory layout


- BUILDING THE SOFTWARE -
=========================
To be able to build the software from its source, you will need a few requirements which are listed below. The JMWiki software is built for Sun's Java
release 1.4.2. 

    requirements
    ============
        - Ant version x.x.x
        - Java SDK version 1.4.2 (or higher)

    building
    ========
    To generate an installable WAR file, simply call ant.

    prompt> ant

    Other ant targets included in the build file are:
        "javadoc" - creates a javadoc set for the JMWiki source code.
        "compile" - only compiles the source code
        "test"    - runs the unit tests
        "clean"   - removes all build artifacts



- DIRECTORY LAYOUT -
====================

This describes the layout of the subversion repository and briefly describes what each directory is for when such explanation is deemed necessary.
The directory layout is as follows:

vqwiki (the root)
   |- lib
   |- src
   |   |- applets
   |   |- java
   |   |    |- org
   |   |        |- vqwiki
   |   |- lex
   |   |    |- alt
   |   |- lib
   |   |- resources
   |   |- webapp
   |        |- META-INF
   |        |- WEB-INF
   |             |- jsp
   |- target (generated during build)

    vqwiki (root)
    =============
    This contains the Ant build files, IntelliJ IDEA project files and this file.

    vqwiki/lib
    ==========
    This contains all the libraries needed for the BUILD PROCESS ONLY. The libraries in this directory are NOT packaged into the final WAR file. This
    allows us to focus on building JMWiki for a certain Java release.

    vqwiki/src/java
    ===============
    Contains all java files belonging to the project. The project files are tucked into a package structure org.vqwiki.*
    
    vqwiki/src/lex
    ==============
    Contains the lexer source files from which the lexer (for example JFlex) will generate the java source code for later use in compilation and building.
    The lex directory only contains the core lexer. The "alt" subdirectory contains any alternative lexers distributed by the JMWiki project.

    vqwiki/src/lib
    ==============
    Contains all the libraries that should be packaged into the final WAR file. These libraries CAN be used in the build process.

    vqwiki/src/webapp
    =================
    All the web application files are located in here conforming to the Maven idea.