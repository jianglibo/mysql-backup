$localZipfile = Get-ChildItem -Path . -Filter "*.zip" -Recurse | Where-Object -Property  FullName -Match "mysql-backup-\d\.\d\.\d+" | Sort-Object -Property FullName -Descending | Select-Object -First 1
$remoteZipfileName = "d:\$($localZipfile.Name)"

$password = get-content C:\cred.txt | convertto-securestring
$credentials = new-object -typename System.Management.Automation.PSCredential -argumentlist "172.19.253.244\Administrator",$password
$session = New-PSSession -ComputerName 172.19.253.244 -Credential $credentials
# $localZipfile | Get-Member
Copy-Item -Path $localZipfile.FullName -Destination d:\ -ToSession $session

Invoke-Command -Session $session -ScriptBlock { param($zf)
    $tmpFolder = "e:\ddd"
    (Test-Path $tmpFolder -PathType Container) -and (Remove-Item $tmpFolder -Force -Recurse) | Out-Null
    New-Item -ItemType directory -Path $tmpFolder -ErrorAction SilentlyContinue | Out-Null
    Expand-Archive -Path $zf -DestinationPath $tmpFolder
    # powershell
    # Get-Process | Where-Object -Property Name -EQ "java" | ForEach-Object {Stop-Process -Id $_.Id}
    # Out-File -FilePath "e:\powershells.txt"
    # Stop-Process 
    # $PID | Out-File -FilePath "e:\pid.txt"
} -ArgumentList $remoteZipfileName

($session -and $session.id ) -and (Remove-PSSession -Id $session.Id) | Out-Null
Get-PSSession
$PID
Get-Process | Where-Object -Property Name -EQ "powershell"