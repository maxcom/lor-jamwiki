
CREATE SEQUENCE vqw_virtual_wiki_seq;

CREATE TABLE vqw_virtual_wiki (
  virtual_wiki_id INTEGER NOT NULL DEFAULT NEXTVAL('vqw_virtual_wiki_seq'),
  virtual_wiki_name VARCHAR(100) NOT NULL,
  create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT vqw_pk_virtual_wiki PRIMARY KEY (virtual_wiki_id)
);

CREATE SEQUENCE vqw_author_seq;

CREATE TABLE vqw_author (
  author_id INTEGER NOT NULL DEFAULT NEXTVAL('vqw_author_seq'),
  login VARCHAR(100) NOT NULL,
  virtual_wiki_id INTEGER NOT NULL,
  confirmation_key VARCHAR(100),
  email VARCHAR(100),
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  display_name VARCHAR(200) NOT NULL,
  encoded_password VARCHAR(100) NOT NULL,
  create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_update_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_login_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  initial_ip_address VARCHAR(15) NOT NULL,
  last_ip_address VARCHAR(15) NOT NULL,
  is_admin BOOLEAN NOT NULL DEFAULT FALSE,
  is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT vqw_pk_author PRIMARY KEY (author_id),
  CONSTRAINT vqw_fk_author_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES vqw_virtual_wiki
);

CREATE SEQUENCE vqw_topic_seq;

CREATE TABLE vqw_topic (
  topic_id INTEGER NOT NULL DEFAULT NEXTVAL('vqw_topic_seq'),
  virtual_wiki_id INTEGER NOT NULL,
  topic_name VARCHAR(200) NOT NULL,
  topic_locked_by INTEGER,
  topic_lock_date TIMESTAMP,
  topic_read_only BOOLEAN DEFAULT FALSE,
  /* standard article, user page, talk page, template */
  topic_type INTEGER NOT NULL,
  CONSTRAINT vqw_pk_topic PRIMARY KEY (topic_id),
  CONSTRAINT vqw_fk_topic_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES vqw_virtual_wiki
);

CREATE SEQUENCE vqw_topic_version_seq;

CREATE TABLE vqw_topic_version (
  topic_version_id INTEGER NOT NULL DEFAULT NEXTVAL('vqw_topic_version_seq'),
  topic_id INTEGER NOT NULL,
  edit_comment VARCHAR(200),
  version_content TEXT,
  author_id INTEGER NOT NULL,
  edit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  edit_type INTEGER NOT NULL,
  CONSTRAINT vqw_pk_topic_version PRIMARY KEY (topic_version_id),
  CONSTRAINT vqw_fk_topic_version_topic FOREIGN KEY (topic_id) REFERENCES vqw_topic,
  CONSTRAINT vqw_fk_topic_version_author FOREIGN KEY (author_id) REFERENCES vqw_author
);

CREATE SEQUENCE vqw_notification_seq;

CREATE TABLE vqw_notification (
  notification_id INTEGER NOT NULL DEFAULT NEXTVAL('vqw_notification_seq'),
  author_id INTEGER NOT NULL,
  topic_id INTEGER NOT NULL,
  CONSTRAINT vqw_pk_notification PRIMARY KEY (notification_id),
  CONSTRAINT vqw_fk_notification_author FOREIGN KEY (author_id) REFERENCES vqw_author,
  CONSTRAINT vqw_fk_notification_topic FOREIGN KEY (topic_id) REFERENCES vqw_topic
);

CREATE SEQUENCE vqw_recent_change_seq;

CREATE TABLE vqw_recent_change (
  recent_change_id INTEGER NOT NULL DEFAULT NEXTVAL('vqw_recent_change_seq'),
  topic_version_id INTEGER NOT NULL,
  topic_id INTEGER NOT NULL,
  topic_name VARCHAR(200) NOT NULL,
  edit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  author_id INTEGER NOT NULL,
  display_name VARCHAR(200) NOT NULL,
  edit_type INTEGER NOT NULL,
  virtual_wiki_id INTEGER NOT NULL,
  virtual_wiki_name VARCHAR(100) NOT NULL,
  CONSTRAINT vqw_pk_recent_change PRIMARY KEY (recent_change_id),
  CONSTRAINT vqw_fk_recent_change_topic_version FOREIGN KEY (topic_version_id) REFERENCES vqw_topic_version,
  CONSTRAINT vqw_fk_recent_change_topic FOREIGN KEY (topic_id) REFERENCES vqw_topic,
  CONSTRAINT vqw_fk_recent_change_author FOREIGN KEY (author_id) REFERENCES vqw_author,
  CONSTRAINT vqw_fk_recent_change_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES vqw_virtual_wiki
);
