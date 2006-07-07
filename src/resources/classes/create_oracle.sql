
CREATE SEQUENCE jam_virtual_wiki_seq;

CREATE TABLE jam_virtual_wiki (
  virtual_wiki_id INTEGER,
  virtual_wiki_name VARCHAR(100) NOT NULL,
  create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT jam_pk_virtual_wiki PRIMARY KEY (virtual_wiki_id)
);

CREATE OR REPLACE TRIGGER jam_trig_virtual_wiki_id
before insert on jam_virtual_wiki for each row
begin
    if :new.virtual_wiki_id is null then
        select jam_virtual_wiki_seq.nextval into :new.virtual_wiki_id from dual;
    end if;
end;

CREATE SEQUENCE jam_wiki_user_seq;

CREATE TABLE jam_wiki_user (
  wiki_user_id INTEGER,
  login VARCHAR(100) NOT NULL,
  display_name VARCHAR(100),
  create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_login_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_ip_address VARCHAR(15) NOT NULL,
  last_login_ip_address VARCHAR(15) NOT NULL,
  is_admin BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT jam_pk_wiki_user PRIMARY KEY (wiki_user_id)
);

CREATE TABLE jam_wiki_user_info (
  wiki_user_id INTEGER NOT NULL,
  login VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  encoded_password VARCHAR(100) NOT NULL,
  CONSTRAINT jam_pk_wiki_user_info PRIMARY KEY (wiki_user_id),
  CONSTRAINT jam_fk_wiki_user_info_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user
);

CREATE OR REPLACE TRIGGER jam_trig_wiki_user_id
before insert on jam_wiki_user for each row
begin
    if :new.wiki_user_id is null then
        select jam_wiki_user_seq.nextval into :new.wiki_user_id from dual;
    end if;
end;

CREATE SEQUENCE jam_topic_seq;

CREATE TABLE jam_topic (
  topic_id INTEGER,
  virtual_wiki_id INTEGER NOT NULL,
  topic_name VARCHAR(200) NOT NULL,
  topic_locked_by INTEGER,
  topic_lock_date TIMESTAMP,
  topic_lock_session_key VARCHAR(100),
  topic_deleted BOOLEAN DEFAULT FALSE,
  topic_read_only BOOLEAN DEFAULT FALSE,
  topic_admin_only BOOLEAN DEFAULT FALSE,
  topic_content TEXT,
  /* standard article, user page, talk page, template */
  topic_type INTEGER NOT NULL,
  CONSTRAINT jam_pk_topic PRIMARY KEY (topic_id),
  CONSTRAINT jam_fk_topic_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES jam_virtual_wiki,
  CONSTRAINT jam_fk_topic_locked_by FOREIGN KEY (topic_locked_by) REFERENCES jam_wiki_user
);

CREATE OR REPLACE TRIGGER jam_trig_topic_id
before insert on jam_topic for each row
begin
    if :new.topic_id is null then
        select jam_topic_seq.nextval into :new.topic_id from dual;
    end if;
end;

CREATE SEQUENCE jam_topic_version_seq;

CREATE TABLE jam_topic_version (
  topic_version_id INTEGER,
  topic_id INTEGER NOT NULL,
  edit_comment VARCHAR(200),
  version_content TEXT,
  wiki_user_id INTEGER,
  wiki_user_ip_address VARCHAR(15) NOT NULL,
  edit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  edit_type INTEGER NOT NULL,
  previous_topic_version_id INTEGER,
  CONSTRAINT jam_pk_topic_version PRIMARY KEY (topic_version_id),
  CONSTRAINT jam_fk_topic_version_topic FOREIGN KEY (topic_id) REFERENCES jam_topic,
  CONSTRAINT jam_fk_topic_version_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user,
  CONSTRAINT jam_fk_topic_version_previous FOREIGN KEY (previous_topic_version_id) REFERENCES jam_topic_version
);

CREATE OR REPLACE TRIGGER jam_trig_topic_version_id
before insert on jam_topic_version for each row
begin
    if :new.topic_version_id is null then
        select jam_topic_version_seq.nextval into :new.topic_version_id from dual;
    end if;
end;

CREATE SEQUENCE jam_notification_seq;

CREATE TABLE jam_notification (
  notification_id INTEGER,
  wiki_user_id INTEGER NOT NULL,
  topic_id INTEGER NOT NULL,
  CONSTRAINT jam_pk_notification PRIMARY KEY (notification_id),
  CONSTRAINT jam_fk_notification_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user,
  CONSTRAINT jam_fk_notification_topic FOREIGN KEY (topic_id) REFERENCES jam_topic
);

CREATE OR REPLACE TRIGGER jam_trig_notification_id
before insert on jam_notification for each row
begin
    if :new.notification_id is null then
        select jam_notification_seq.nextval into :new.notification_id from dual;
    end if;
end;

CREATE SEQUENCE jam_recent_change_seq;

CREATE TABLE jam_recent_change (
  recent_change_id INTEGER,
  topic_version_id INTEGER NOT NULL,
  previous_topic_version_id INTEGER,
  topic_id INTEGER NOT NULL,
  topic_name VARCHAR(200) NOT NULL,
  edit_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  edit_comment VARCHAR(200),
  wiki_user_id INTEGER,
  display_name VARCHAR(200) NOT NULL,
  edit_type INTEGER NOT NULL,
  virtual_wiki_id INTEGER NOT NULL,
  virtual_wiki_name VARCHAR(100) NOT NULL,
  CONSTRAINT jam_pk_recent_change PRIMARY KEY (recent_change_id),
  CONSTRAINT jam_fk_recent_change_topic_version FOREIGN KEY (topic_version_id) REFERENCES jam_topic_version,
  CONSTRAINT jam_fk_recent_change_previous_topic_version FOREIGN KEY (previous_topic_version_id) REFERENCES jam_topic_version,
  CONSTRAINT jam_fk_recent_change_topic FOREIGN KEY (topic_id) REFERENCES jam_topic,
  CONSTRAINT jam_fk_recent_change_wiki_user FOREIGN KEY (wiki_user_id) REFERENCES jam_wiki_user,
  CONSTRAINT jam_fk_recent_change_virtual_wiki FOREIGN KEY (virtual_wiki_id) REFERENCES jam_virtual_wiki
);

CREATE OR REPLACE TRIGGER jam_trig_recent_change_id
before insert on jam_recent_change for each row
begin
    if :new.recent_change_id is null then
        select jam_recent_change_seq.nextval into :new.recent_change_id from dual;
    end if;
end;
