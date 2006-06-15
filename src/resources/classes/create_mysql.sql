      CREATE TABLE IF NOT EXISTS Topic(
      name VARCHAR(100) NOT NULL,
      contents TEXT,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( name, virtualwiki) );

      CREATE TABLE IF NOT EXISTS TopicVersion(
      name VARCHAR(100) NOT NULL,
      contents TEXT,
      versionat DATETIME NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY (name, versionat, virtualwiki));

      CREATE TABLE IF NOT EXISTS TopicChange(
      topic VARCHAR(100) NOT NULL,
      username VARCHAR(100),
      changeat DATETIME NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( topic, changeat, virtualwiki ) );

      CREATE TABLE IF NOT EXISTS TopicLock(
      topic VARCHAR(100) NOT NULL,
      sessionkey VARCHAR(100) NOT NULL,
      lockat DATETIME,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( topic, sessionkey, virtualwiki ) );

      CREATE TABLE IF NOT EXISTS TopicReadOnly(
      topic VARCHAR(100) NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( topic, virtualwiki ) );

      CREATE TABLE IF NOT EXISTS VirtualWiki(
      name VARCHAR(100) NOT NULL,
      PRIMARY KEY( name ) );

      CREATE TABLE IF NOT EXISTS WikiTemplate(
      name VARCHAR(100) NOT NULL,
      contents TEXT,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( name, virtualwiki )
      );

      CREATE TABLE IF NOT EXISTS Notification(
      topic VARCHAR(100) NOT NULL,
      user VARCHAR(100) NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      PRIMARY KEY( topic, user, virtualwiki )
      );

      CREATE TABLE IF NOT EXISTS Member(
      user VARCHAR(100) NOT NULL,
      virtualwiki VARCHAR(100) NOT NULL,
      email VARCHAR(100),
      userkey VARCHAR(100),
      PRIMARY KEY( user, virtualwiki )
      );