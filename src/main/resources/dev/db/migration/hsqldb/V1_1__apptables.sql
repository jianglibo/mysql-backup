CREATE TABLE server
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  name VARChAR(256) NOT NULL,
  core_number INTEGER,
  host VARCHAR(200) NOT NULL,
  port INTEGER NOT NULL,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(64),
  ssh_key_file VARCHAR(256),
  server_state_cron VARCHAR(256),
  storage_state_cron VARCHAR(256),
  server_role VARCHAR(32) DEFAULT 'GET',
  os VARCHAR(64),
  created_at TIMESTAMP(2),
  CONSTRAINT unique_server_host UNIQUE (host)
);

CREATE TABLE mysql_instance
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  server_id INTEGER NOT NULL,
  host VARCHAR(200) NOT NULL,
  port INTEGER NOT NULL,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(64) NOT NULL,
  mycnf_file VARCHAR(256),
  flush_log_cron VARCHAR(128),
  mysql_settings VARCHAR(256) ARRAY DEFAULT ARRAY[],
  dump_file_name VARCHAR(256),
  client_bin VARCHAR(256),
  created_at TIMESTAMP(2),
  CONSTRAINT fk_mysql_instance_server FOREIGN KEY (server_id)  REFERENCES server (id),
  CONSTRAINT unique_mysql_instance_server_id UNIQUE (server_id)
);


CREATE TABLE borg_description
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  server_id INTEGER NOT NULL,
  repo VARCHAR(256) DEFAULT '/opt/borgrepos/repo',
  includes VARCHAR(256) ARRAY DEFAULT ARRAY[],
  excludes VARCHAR(256) ARRAY DEFAULT ARRAY[],
  archive_format VARCHAR(128) DEFAULT 'yyyy-MM-dd-HH-mm-ss',
  archive_cron VARCHAR(128),
  prune_cron VARCHAR(128),
  archive_name_prefix VARCHAR(32) DEFAULT 'ARCHIVE-',
  created_at TIMESTAMP(2),
  CONSTRAINT fk_borg_description_server FOREIGN KEY (server_id)  REFERENCES server (id),
  CONSTRAINT unique_borg_description_server_id UNIQUE (server_id)
);

--ALTER TABLE server ADD CONSTRAINT unique_host UNIQUE (host);

CREATE TABLE server_grp
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  ename VARCHAR(256) NOT NULL,
  msgkey VARCHAR(256),
  created_at TIMESTAMP(2),
  CONSTRAINT unique_servergrp_ename UNIQUE (ename)
);

CREATE TABLE servergrp_and_server (
  server_id INT  NOT NULL,
  grp_id    INT  NOT NULL,
  PRIMARY KEY (server_id, grp_id),
  CONSTRAINT fk_sgs_server     FOREIGN KEY (server_id)  REFERENCES server (id),
  CONSTRAINT fk_sgs_grp       FOREIGN KEY (grp_id)    REFERENCES server_grp   (id)
);

CREATE TABLE backup_folder
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  server_id INTEGER NOT NULL,
  folder VARCHAR(1000) NOT NULL,
  CONSTRAINT fk_backup_folder_server FOREIGN KEY (server_id)  REFERENCES server (id),
  CONSTRAINT uni_bf_server_folder UNIQUE (server_id, folder)
);

--ALTER TABLE backup_folder ADD CONSTRAINT unique_server_folder UNIQUE (server_id, folder);

CREATE TABLE backup_folder_state
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  backup_folder_id INTEGER NOT NULL,
  how_many INTEGER,
  total_size_in_kb INTEGER,
  created_at TIMESTAMP(2),
  CONSTRAINT fk_backup_folder FOREIGN KEY (backup_folder_id)  REFERENCES backup_folder (id)
);

CREATE TABLE mysql_dump
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  server_id INTEGER NOT NULL,
  result VARCHAR(20),
  time_cost BIGINT,
  created_at TIMESTAMP(2),
  
  file_size BIGINT,
  CONSTRAINT fk_mysqldump_server FOREIGN KEY (server_id)  REFERENCES server (id)
);

CREATE TABLE mysql_flush
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  server_id INTEGER NOT NULL,
  result VARCHAR(20),
  time_cost BIGINT,
  created_at TIMESTAMP(2),
  
  file_size BIGINT,
  file_number INTEGER,

  CONSTRAINT fk_mysqlflush_server FOREIGN KEY (server_id)  REFERENCES server (id)
);

CREATE TABLE borg_download
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  server_id INTEGER NOT NULL,
  created_at TIMESTAMP(2),
  result VARCHAR(20),
  time_cost BIGINT,
  
  total_files INTEGER,
  download_files INTEGER,
  total_bytes BIGINT,
  download_bytes BIGINT,
  CONSTRAINT fk_borg_download_server FOREIGN KEY (server_id)  REFERENCES server (id)
);

CREATE INDEX bd_created_at_idx ON borg_download(created_at);
CREATE INDEX md_created_at_idx ON mysql_dump(created_at);
CREATE INDEX mf_created_at_idx ON mysql_flush(created_at);
CREATE INDEX bfs_created_at_idx ON backup_folder_state(created_at);


CREATE TABLE reuseable_cron (
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  description     VARCHAR(200),
  expression      VARCHAR(200)  NOT NULL,
  created_at TIMESTAMP(2),
  CONSTRAINT unique_rc_expression UNIQUE (expression)
);

 CREATE TABLE user_account (
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  name  VARCHAR(200)  NOT NULL,
  mobile  VARCHAR(64)  NOT NULL,
  email      VARCHAR(200)  NOT NULL,
  description     VARCHAR(200),
  created_at TIMESTAMP(2),
  CONSTRAINT unique_ua_email UNIQUE (email),
  CONSTRAINT unique_ua_name UNIQUE (name),
  CONSTRAINT unique_ua_mobile UNIQUE (mobile)
);

CREATE TABLE user_grp
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  ename VARCHAR(256),
  msgkey VARCHAR(256),
  created_at TIMESTAMP(2),
  CONSTRAINT unique_usergrp_ename UNIQUE (ename)
);

CREATE TABLE usergrp_and_user (
  user_id INT  NOT NULL,
  grp_id    INT  NOT NULL,
  PRIMARY KEY (user_id, grp_id),
  CONSTRAINT fk_ugu_user     FOREIGN KEY (user_id)  REFERENCES user_account (id),
  CONSTRAINT fk_ugu_grp       FOREIGN KEY (grp_id)    REFERENCES user_grp   (id)
);

CREATE TABLE user_and_server_grp
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  user_account_id INTEGER NOT NULL,
  server_grp_id INTEGER NOT NULL,
  name VARChAR(256) NOT NULL,
  template VARCHAR(64),
  cron_expression VARChAR(256) NOT NULL,
  created_at TIMESTAMP(2),
  CONSTRAINT fk_user_servergrp_grp_id FOREIGN KEY (server_grp_id)  REFERENCES server_grp (id),
  CONSTRAINT fk_user_servergrp_user_id FOREIGN KEY (user_account_id)  REFERENCES user_account (id),
  CONSTRAINT unique_usg_u_sg UNIQUE (user_account_id, server_grp_id)
);

CREATE TABLE big_ob
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  server_id INTEGER NOT NULL,
  name VARChAR(256) NOT NULL,
  description VARCHAR(256),
  content BLOB,
  created_at TIMESTAMP(2),
  CONSTRAINT unique_big_ob_name UNIQUE (name)
);

CREATE TABLE storage_state
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  server_id INTEGER NOT NULL,
  root VARChAR(128) NOT NULL,
  used BIGINT,
  available BIGINT,
  created_at TIMESTAMP(2),
  CONSTRAINT fk_storage_state_server FOREIGN KEY (server_id)  REFERENCES server (id)
);

CREATE TABLE server_state
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  server_id INTEGER NOT NULL,
  mem_free BIGINT,
  mem_used BIGINT,
  average_load INTEGER,
  created_at TIMESTAMP(2),
  CONSTRAINT fk_server_state_server FOREIGN KEY (server_id)  REFERENCES server (id)
);

CREATE TABLE job_log
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  job_class VARCHAR(128),
  ctx VARCHAR(256),
  exp BLOB,
  created_at TIMESTAMP(2)
);

CREATE TABLE play_back
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  source_server_id INTEGER NOT NULL,
  target_server_id INTEGER NOT NULL,
  play_what VARCHAR(64) NOT NULL,
  pairs VARCHAR(256) ARRAY DEFAULT ARRAY[],
  created_at TIMESTAMP(2),
  CONSTRAINT unique_play_back UNIQUE (source_server_id, target_server_id, play_what)
);

CREATE TABLE key_value
(
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  item_key VARCHAR(256) NOT NULL,
  item_value VARCHAR(256) NOT NULL,
  created_at TIMESTAMP(2),
  CONSTRAINT unique_key_value_key UNIQUE (item_key)
);

