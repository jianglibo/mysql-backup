## 明明白白地备份MYSQL

按以下方式备份：

* 执行mysqldump --all-databases --flush-logs --master-data=2 > backup.sql
* 执行FLUSH_BIN_LOG，同时备份binlog文件。

gradle packmyzip

configuration file has a "taskcmd" field that describing the tasks.

ConfigFileLoader, getProcessBuilderNeededList(), PSUtil.invokePowershell

ProcessExecResult per = configFileLoader.runCommand(borgconfigfile, "archive");

The other configuration items in file application-prod.yml. 
psdataDir,
psapp,
downloadFolder,

manually execute scripts:
PS D:\easy-installers> .\easy-installer\scripts\mysql\mysql-client-side.ps1 -Action FlushLogFileHash -ConfigFile .\configdir\configs\10.19.183.53\mysql-config.json -Verbose