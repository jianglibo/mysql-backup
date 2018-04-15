## 明明白白地备份MYSQL

按以下方式备份：

* 执行mysqldump --all-databases --flush-logs --master-data=2 > backup.sql
* 执行FLUSH_BIN_LOG，同时备份binlog文件。