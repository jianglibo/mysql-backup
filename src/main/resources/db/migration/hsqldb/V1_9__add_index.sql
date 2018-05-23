CREATE INDEX bd_created_at_idx ON borg_download(created_at);
CREATE INDEX md_created_at_idx ON mysql_dump(created_at);
CREATE INDEX mf_created_at_idx ON mysql_flush(created_at);
CREATE INDEX df_created_at_idx ON diskfree(created_at);
CREATE INDEX ut_created_at_idx ON up_time(created_at);
CREATE INDEX bfs_created_at_idx ON backup_folder_state(created_at);