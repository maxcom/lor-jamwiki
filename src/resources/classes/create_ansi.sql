      CREATE TABLE  Topic(
      name VARCHAR(100) NOT NULL,
      contents TEXT,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( name, virtualwiki) );

      CREATE TABLE  TopicVersion(
      name VARCHAR(100) NOT NULL,
      contents TEXT,
      versionat DATETIME NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY (name, versionat, virtualwiki));

      CREATE TABLE  TopicChange(
      topic VARCHAR(100) NOT NULL,
      username VARCHAR(100),
      changeat DATETIME NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( topic, changeat, virtualwiki ) );

      CREATE TABLE  TopicLock(
      topic VARCHAR(100) NOT NULL,
      sessionkey VARCHAR(100) NOT NULL,
      lockat DATETIME,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( topic, sessionkey, virtualwiki ) );

      CREATE TABLE TopicReadOnly(
      topic VARCHAR(100) NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( topic, virtualwiki ) );

      CREATE TABLE VirtualWiki(
      name VARCHAR(100) NOT NULL,
      PRIMARY KEY( name ) );

      CREATE TABLE WikiTemplate(
      name VARCHAR(100) NOT NULL,
      contents TEXT,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( name, virtualwiki )
      );

      CREATE TABLE Notification(
      topic VARCHAR(100) NOT NULL,
      wikiuser VARCHAR(100) NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( topic, wikiuser, virtualwiki )
      );

      CREATE TABLE WikiMember(
      wikiuser VARCHAR(100) NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      email VARCHAR(100),
      userkey VARCHAR(100),
      PRIMARY KEY( wikiuser, virtualwiki )
      );