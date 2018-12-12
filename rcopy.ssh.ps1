Param(
    [ValidateSet("Administrator@172.19.253.244:D:\\easy-installers")]
    [string]$RemotePath
)

$localZipfile = Get-ChildItem -Path . -Filter "*.zip" -Recurse | Where-Object -Property  FullName -Match "mysql-backup-\d\.\d\.\d+" | Sort-Object -Property FullName -Descending | Select-Object -First 1

$cpcmd = "scp {0} {1}" -f $localZipfile.FullName, $RemotePath

Invoke-Expression -Command $cpcmd