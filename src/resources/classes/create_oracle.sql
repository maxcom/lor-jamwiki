
CREATE SEQUENCE jmw_virtual_wiki_seq;

CREATE TABLE jmw_virtual_wiki (
  virtual_wiki_id INTEGER,
  virtual_wiki_name VARCHAR(100) NOT NULL,
  create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT jmw_pk_virtual_wiki PRIMARY KEY (virtual_wiki_id)
);

CREATE OR REPLACE TRIGGER jmw_trig_virtual_wiki_id
before insert on jmw_virtual_wiki for each row
begin
    if :new.virtual_wiki_id is null then
        select jmw_virtual_wiki_seq.nextval into :new.virtual_wiki_id from dual;
    end if;
end;

CREATE SEQUENCE jmw_author_seq;

CREATE TABLE jmw_author (
  author_id INTEGER,
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
  CONSTRAINT jmw_pk_author PRIMARY KEY (author_id),
  CONSTRAINT jmw_fk_author_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES jmw_virtual_wiki
);

CREATE OR REPLACE TRIGGER jmw_trig_author_id
before insert on jmw_author for each row
begin
    if :new.author_id is null then
        select jmw_author_seq.nextval into :new.author_id from dual;
    end if;
end;

CREATE SEQUENCE jmw_topic_seq;

CREATE TABLE jmw_topic (
  topic_id INTEGER,
  virtual_wiki_id INTEGER NOT NULL,
  topic_name VARCHAR(200) NOT NULL,
  topic_locked_by INTEGER,
  topic_lock_date TIMESTAMP,
  topic_read_only BOOLEAN DEFAULT FALSE,
  /* standard article, user page, talk page, template */
  topic_type INTEGER NOT NULL,
  CONSTRAINT jmw_pk_topic PRIMARY KEY (topic_id),
  CONSTRAINT jmw_fk_topic_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES jmw_virtual_wiki
);

CREATE OR REPLACE TRIGGER jmw_trig_topic_id
before insert on jmw_topic for each row
begin
    if :new.topic_id is null then
        select jmw_topic_seq.nextval into :new.topic_id from dual;
    end if;
end;

CREATE SEQUENCE jmw_topic_version_seq;

CREATE TABLE jmw_topic_version (
  topic_version_id INTEGER NOT NULL DEFAULT NEXTVAL('jmw_topic_version_seq'),
  topic_id INTEGER NOT NULL,
  edit_comment VARCHAR(200),
  version_content TEXT,
  author_id INTEGER NOT NULL,
  edit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  edit_type INTEGER NOT NULL,
  CONSTRAINT jmw_pk_topic_version PRIMARY KEY (topic_version_id),
  CONSTRAINT jmw_fk_topic_version_topic FOREIGN KEY (topic_id) REFERENCES jmw_topic,
  CONSTRAINT jmw_fk_topic_version_author FOREIGN KEY (author_id) REFERENCES jmw_author
);

CREATE OR REPLACE TRIGGER jmw_trig_topic_version_id
before insert on jmw_topic_version for each row
begin
    if :new.topic_version_id is null then
        select jmw_topic_version_seq.nextval into :new.topic_version_id from dual;
    end if;
end;

CREATE SEQUENCE jmw_notification_seq;

CREATE TABLE jmw_notification (
  notification_id INTEGER,
  author_id INTEGER NOT NULL,
  topic_id INTEGER NOT NULL,
  CONSTRAINT jmw_pk_notification PRIMARY KEY (notification_id),
  CONSTRAINT jmw_fk_notification_author FOREIGN KEY (author_id) REFERENCES jmw_author,
  CONSTRAINT jmw_fk_notification_topic FOREIGN KEY (topic_id) REFERENCES jmw_topic
);

CREATE OR REPLACE TRIGGER jmw_trig_notification_id
before insert on jmw_notification for each row
begin
    if :new.notification_id is null then
        select jmw_notification_seq.nextval into :new.notification_id from dual;
    end if;
end;

CREATE SEQUENCE jmw_recent_change_seq;

CREATE TABLE jmw_recent_change (
  recent_change_id INTEGER,
  topic_version_id INTEGER NOT NULL,
  topic_id INTEGER NOT NULL,
  topic_name VARCHAR(200) NOT NULL,
  edit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  author_id INTEGER NOT NULL,
  display_name VARCHAR(200) NOT NULL,
  edit_type INTEGER NOT NULL,
  virtual_wiki_id INTEGER NOT NULL,
  virtual_wiki_name VARCHAR(100) NOT NULL,
  CONSTRAINT jmw_pk_recent_change PRIMARY KEY (recent_change_id),
  CONSTRAINT jmw_fk_recent_change_topic_version FOREIGN KEY (topic_version_id) REFERENCES jmw_topic_version,
  CONSTRAINT jmw_fk_recent_change_topic FOREIGN KEY (topic_id) REFERENCES jmw_topic,
  CONSTRAINT jmw_fk_recent_change_author FOREIGN KEY (author_id) REFERENCES jmw_author,
  CONSTRAINT jmw_fk_recent_change_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES jmw_virtual_wiki
);

CREATE OR REPLACE TRIGGER jmw_trig_recent_change_id
before insert on jmw_recent_change for each row
begin
    if :new.recent_change_id is null then
        select jmw_recent_change_seq.nextval into :new.recent_change_id from dual;
    end if;
end;
