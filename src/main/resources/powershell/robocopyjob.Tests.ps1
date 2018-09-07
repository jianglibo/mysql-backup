$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$sut = (Split-Path -Leaf $MyInvocation.MyCommand.Path) -replace '\.Tests\.', '.'
. "$here\$sut"


function hp() {
    Param(
        [Parameter(Mandatory=$true)]
        [hashtable[]]
        $ht
    )
    $ht
}

Describe "hashtable as parameter" {
    It "shoud be hash." {
       (hp -ht @{a=1;b=2}, @{c=2;d=3;e=4}, @{}).Length | Should -Be 3
    }
}

Describe "json should work" {
    It "works" {
        # $str = '{"id":null,"createdAt":"2018-09-07T00:37:10.804+0000","repo":"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\junit5208538294238145161","invokeCron":null,"localBackupCron":null,"pruneStrategy":"0 0 2 7 4 1 1","serverId":201,"compressCommand":"& \'C:/Program Files/WinRAR/Rar.exe\' a -ms %s %s","expandCommand":"& \'C:/Program Files/WinRAR/Rar.exe\' x -o+ %s %s","alwaysFullBackup":false,"archiveName":"hello.rar","robocopyItems":[{"id":null,"createdAt":"2018-09-07T00:37:10.804+0000","descriptionId":0,"source":"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\junit8840978354135678372","dstRelative":"abc","fileParameters":"*.*","excludeFiles":[],"excludeDirectories":[],"copyOptions":[],"fileSelectionOptions":[],"retryOptions":[],"loggingOptions":[],"jobOptions":[],"fileParametersNullSafe":"*.*","excludeDirectoriesNullSafe":[],"copyOptionsNullSafe":["/e"],"loggingOptionsNullSafe":["/fp","/njh","/njs"],"fileSelectionOptionsNullSafe":[],"excludeFilesNullSafe":[],"jobOptionsNullSafe":[],"retryOptionsNullSafe":[]},{"id":null,"createdAt":"2018-09-07T00:37:10.804+0000","descriptionId":0,"source":"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\junit8840978354135678372","dstRelative":"abc1","fileParameters":"*.*","excludeFiles":[],"excludeDirectories":[],"copyOptions":[],"fileSelectionOptions":[],"retryOptions":[],"loggingOptions":[],"jobOptions":[],"fileParametersNullSafe":"*.*","excludeDirectoriesNullSafe":[],"copyOptionsNullSafe":["/e"],"loggingOptionsNullSafe":["/fp","/njh","/njs"],"fileSelectionOptionsNullSafe":[],"excludeFilesNullSafe":[],"jobOptionsNullSafe":[],"retryOptionsNullSafe":[]}],"workingSpaceExpanded":"C:/Users/ADMINI~1/AppData/Local/Temp/junit5208538294238145161/workingspace/expanded","workingSpaceCompressedArchive":"C:/Users/ADMINI~1/AppData/Local/Temp/junit5208538294238145161/workingspace/compressed/hello.rar","workingSpaceScriptFile":"C:/Users/ADMINI~1/AppData/Local/Temp/junit5208538294238145161/workingspace/robocopy.ps1","workingSpaceRoboLog":"C:/Users/ADMINI~1/AppData/Local/Temp/junit5208538294238145161/workingspace/robocopy.log","robocopyDst":"C:/Users/ADMINI~1/AppData/Local/Temp/junit5208538294238145161/robocopydst"}'
        # $str = '"compressCommand":"& \'C:/Program Files/WinRAR/Rar.exe`' a -ms %s %s","expandCommand":"& `'C:/Program Files/WinRAR/Rar.exe`' x -o+ %s %s"';
        $str = "{\"id\":null,\"createdAt\":\"2018-09-07T00:43:20.973+0000\",\"repo\":\"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\junit407297517316928943\",\"invokeCron\":null,\"localBackupCron\":null,\"pruneStrategy\":\"0 0 2 7 4 1 1\",\"serverId\":205,\"compressCommand\":\"& 'C:/Program Files/WinRAR/Rar.exe' a -ms %s %s\",\"expandCommand\":\"& 'C:/Program Files/WinRAR/Rar.exe' x -o+ %s %s\",\"alwaysFullBackup\":false,\"archiveName\":\"hello.rar\",\"robocopyItems\":[{\"id\":null,\"createdAt\":\"2018-09-07T00:43:20.973+0000\",\"descriptionId\":0,\"source\":\"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\junit7165599751604458805\",\"dstRelative\":\"abc\",\"fileParameters\":\"*.*\",\"excludeFiles\":[],\"excludeDirectories\":[],\"copyOptions\":[],\"fileSelectionOptions\":[],\"retryOptions\":[],\"loggingOptions\":[],\"jobOptions\":[],\"fileSelectionOptionsNullSafe\":[],\"loggingOptionsNullSafe\":[\"/fp\",\"/njh\",\"/njs\"],\"excludeFilesNullSafe\":[],\"excludeDirectoriesNullSafe\":[],\"copyOptionsNullSafe\":[\"/e\"],\"fileParametersNullSafe\":\"*.*\",\"jobOptionsNullSafe\":[],\"retryOptionsNullSafe\":[]},{\"id\":null,\"createdAt\":\"2018-09-07T00:43:20.973+0000\",\"descriptionId\":0,\"source\":\"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\junit7165599751604458805\",\"dstRelative\":\"abc1\",\"fileParameters\":\"*.*\",\"excludeFiles\":[],\"excludeDirectories\":[],\"copyOptions\":[],\"fileSelectionOptions\":[],\"retryOptions\":[],\"loggingOptions\":[],\"jobOptions\":[],\"fileSelectionOptionsNullSafe\":[],\"loggingOptionsNullSafe\":[\"/fp\",\"/njh\",\"/njs\"],\"excludeFilesNullSafe\":[],\"excludeDirectoriesNullSafe\":[],\"copyOptionsNullSafe\":[\"/e\"],\"fileParametersNullSafe\":\"*.*\",\"jobOptionsNullSafe\":[],\"retryOptionsNullSafe\":[]}],\"workingSpaceCompressedArchive\":\"C:/Users/ADMINI~1/AppData/Local/Temp/junit407297517316928943/workingspace/compressed/hello.rar\",\"workingSpaceExpanded\":\"C:/Users/ADMINI~1/AppData/Local/Temp/junit407297517316928943/workingspace/expanded\",\"workingSpaceScriptFile\":\"C:/Users/ADMINI~1/AppData/Local/Temp/junit407297517316928943/workingspace/robocopy.ps1\",\"workingSpaceRoboLog\":\"C:/Users/ADMINI~1/AppData/Local/Temp/junit407297517316928943/workingspace/robocopy.log\",\"robocopyDst\":\"C:/Users/ADMINI~1/AppData/Local/Temp/junit407297517316928943/robocopydst\"}"
        $ob = $str | ConvertFrom-Json
        $ob | Select-Object  pruneStrategy | Should -Be "0 0 2 7 4 1 1"

    }
}

Describe "robocopyjob" {
    It "return list of robocopies" {
        $repo = "TestDrive:\repo"
        $repo | RobocopyExecutables -dirpair @{src="c:\abc";dst="d1"} | Should -Be 'abdc'
    }

    It "does something useful" {
        $repo = "TestDrive:\repo"
        New-Item -ItemType Directory -Path $repo
        $testfile = Join-Path -Path $repo -ChildPath "abc.txt"
        1 | Out-File $testfile
        New-Item -ItemType Directory -Path $repo -Force
        Test-Path $testfile | Should -Be $true
        Get-Content $testfile | Should -Be "1"
        Test-Path $repo | Should -Be $true
    }

    It "shoud create sub directories" {
        $repo = "TestDrive:\ttt\repo"
        $repo | Robocopyjob -dirpair @{a=1;b=2}

        "robocobydst", "compressed", "log" |ForEach-Object  {Join-Path -Path $repo -ChildPath $_} | Test-Path | Should -Be @($true,$true,$true)
        Get-Content -Path (Join-Path $repo -ChildPath number.txt) | Should -BeExactly "0"

    }
}
