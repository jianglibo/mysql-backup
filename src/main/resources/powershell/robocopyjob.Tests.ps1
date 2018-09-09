$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$sut = (Split-Path -Leaf $MyInvocation.MyCommand.Path) -replace '\.Tests\.', '.'
. "$here\$sut"

$v = @"
    {"id":null,"createdAt":"2018-09-07T02:46:45.002+0000",
    "repo":"TestDrive:/repo","invokeCron":null,"localBackupCron":null,
    "pruneStrategy":"0 0 2 7 4 1 1","serverId":147,
    "compressCommand":"& 'C:/Program Files/WinRAR/Rar.exe' a -ms %s %s",
    "compressExe": "C:/Program Files/WinRAR/Rar.exe",
    "expandCommand":"& 'C:/Program Files/WinRAR/Rar.exe' x -o+ %s %s",
    "alwaysFullBackup":false,"archiveName":"hello.rar",
    "robocopyItems":[
        {"id":null,"createdAt":"2018-09-07T02:46:45.002+0000","descriptionId":0,
            "source":"TestDrive:/src/1",
            "dstRelative":"abc",
            "fileParameters":"*.*","excludeFiles":[],"excludeDirectories":[],"copyOptions":[],"fileSelectionOptions":[],"retryOptions":[],"loggingOptions":[],"jobOptions":[],"excludeDirectoriesNullSafe":[],"fileParametersNullSafe":"*.*","excludeFilesNullSafe":[],"copyOptionsNullSafe":["/e"],"fileSelectionOptionsNullSafe":[],"loggingOptionsNullSafe":["/fp","/njh","/njs"],"retryOptionsNullSafe":[],"jobOptionsNullSafe":[]},
        {"id":null,"createdAt":"2018-09-07T02:46:45.002+0000",
            "descriptionId":0,
            "source":"TestDrive:/src/2",
            "dstRelative":"abc1",
            "fileParameters":"*.*","excludeFiles":[],"excludeDirectories":[],"copyOptions":[],"fileSelectionOptions":[],"retryOptions":[],"loggingOptions":[],"jobOptions":[],"excludeDirectoriesNullSafe":[],"fileParametersNullSafe":"*.*","excludeFilesNullSafe":[],"copyOptionsNullSafe":["/e"],"fileSelectionOptionsNullSafe":[],"loggingOptionsNullSafe":["/fp","/njh","/njs"],"retryOptionsNullSafe":[],"jobOptionsNullSafe":[]}
    ],
    "workingSpaceScriptFile":"TestDrive:/repo/workingspace/robocopy.ps1",
    "workingSpaceCompressedArchive":"TestDrive:/repo/workingspace/hello.rar",
    "workingSpaceExpanded":"TestDrive:/repo/workingspace/expanded",
    "workingSpaceRoboLog":"TestDrive:/repo/workingspace/robocopy.log",
    "workingSpaceChangeList":"TestDrive:/repo/workingspace/changelist.txt",
    "workingSpace":"TestDrive:/repo/workingspace",
    "compressCommandInstance": "& 'C:/Program Files/WinRAR/Rar.exe' a -ms TestDrive:/repo/workingspace/compressed/hello.rar TestDrive:/repo/robocopydst;$LASTEXITCODE",
    "workingSpaceIncreamentalArchive": "TestDrive:/repo/workingspace/increamental.rar",
    "robocopyDst":"TestDrive:/repo/robocopydst"}
"@

function RobocopyExecutables {
    Param(
        [parameter(Mandatory = $true, Position = 1)]
        [RobocopyDescription]$RobocopyDescription
    )
    foreach ($ht in $RobocopyDescription.robocopyItems) {
        $v = "Robocopy.exe {0} {1} /e /fp /njh /njs /log+:{2}" -f $ht.source, (Join-Path -Path $RobocopyDescription.robocopyDst -ChildPath $ht.dstRelative), $RobocopyDescription.workingSpaceRoboLog | Out-String
        $v.Trim()
    }
}

function hp() {
    Param(
        [Parameter(Mandatory = $true)]
        [hashtable[]]
        $ht
    )
    $ht
}

Describe "hashtable as parameter" {
    It "shoud be hash." {
        (hp -ht @{a = 1; b = 2}, @{c = 2; d = 3; e = 4}, @{}).Length | Should -Be 3
    }

    It "should be array assign" {
        $robocopies = 'Robocopy.exe C:/Users/admin/AppData/Local/Temp/junit1530064060961660901 C:/Users/admin/AppData/Local/Temp/junit6917332990589298536/robocopydst/abc *.* /log+:C:/Users/admin/AppData/Local/Temp/junit6917332990589298536/workingspace/robocopy.log /e /fp /njh /njs,Robocopy.exe C:/Users/admin/AppData/Local/Temp/junit1530064060961660901 C:/Users/admin/AppData/Local/Temp/junit6917332990589298536/robocopydst/abc1 *.* /log+:C:/Users/admin/AppData/Local/Temp/junit6917332990589298536/workingspace/robocopy.log /e /fp /njh /njs'
        $robocopies -split "," | Measure-Object | Select-Object -ExpandProperty Count | Should -Be 2
    }
}

Describe "json should work" {
    $src1 = "TestDrive:\src\1"
    New-Item -ItemType Directory -Path $src1
    "abc" | Out-File -FilePath (Join-Path $src1 -ChildPath "level1_1.txt")
    $adir = Join-Path -Path $src1 -ChildPath "levelonedir"
    New-Item -ItemType Directory -Path $adir
    "abc" | Out-File -FilePath (Join-Path $adir -ChildPath "level2_1.txt")

    $src2 = "TestDrive:\src\2"
    New-Item -ItemType Directory -Path $src2
    "abc" | Out-File -FilePath (Join-Path $src2 -ChildPath "level2_1.txt")
    $adir = Join-Path -Path $src2 -ChildPath "levelonedir"
    New-Item -ItemType Directory -Path (Join-Path -Path $src2 -ChildPath "levelonedir")
    "abc" | Out-File -FilePath (Join-Path $adir -ChildPath "level2_2.txt")

    $repo = "TestDrive:\repo"
    "robocobydst", "workingspace/compressed" | ForEach-Object {Join-Path -Path $repo -ChildPath $_} | Where-Object {-not (Test-Path $_)} | ForEach-Object {New-Item -ItemType Directory -Path $_}

    It "is magic here string" {
        $RobocopyDescriptionJsonValue = $v | ConvertFrom-Json
        $RobocopyDescriptionJsonValue | Should -BeOfType [PSCustomObject]
        $RobocopyDescriptionJsonValue | Select-Object  -ExpandProperty pruneStrategy | Should -Be "0 0 2 7 4 1 1"
        [RobocopyDescription]$RobocopyDsc = ConvertTo-RobocopyDescription $RobocopyDescriptionJsonValue

        $RobocopyDsc.workingSpaceChangeList = $RobocopyDsc.workingSpaceChangeList -replace "TestDrive:", $TestDrive
        $RobocopyDsc.workingSpaceIncreamentalArchive = $RobocopyDsc.workingSpaceIncreamentalArchive -replace "TestDrive:", $TestDrive

        $roboexecs = RobocopyExecutables ($RobocopyDsc -as [RobocopyDescription])
        $roboexecs | Should -Be @('Robocopy.exe TestDrive:/src/1 TestDrive:\repo\robocopydst\abc /e /fp /njh /njs /log+:TestDrive:/repo/workingspace/robocopy.log',
            'Robocopy.exe TestDrive:/src/2 TestDrive:\repo\robocopydst\abc1 /e /fp /njh /njs /log+:TestDrive:/repo/workingspace/robocopy.log')

        $roboexecs = $roboexecs | ForEach-Object {$_ -replace "TestDrive:", $TestDrive}

        New-Item -Path $RobocopyDsc.workingspace -Type Directory -ErrorAction SilentlyContinue

        Test-Path $RobocopyDsc.workingSpaceRoboLog | Should -Be $false
        Test-Path $RobocopyDsc.workingSpaceIncreamentalArchive | Should -Be $false
        Test-Path $RobocopyDsc.workingSpaceChangeList | Should -Be $false
        $output = Start-Robocopy $RobocopyDsc $roboexecs

        $output[1] | Should -Be 0

        Test-Path $output[0] | Should -Be $true

        Test-Path $RobocopyDsc.workingSpaceRoboLog | Should -Be $true
        Test-Path $RobocopyDsc.workingSpaceIncreamentalArchive | Should -Be $true
        Test-Path $RobocopyDsc.workingSpaceChangeList | Should -Be $true

        # run again
        $output = Start-Robocopy $RobocopyDsc $roboexecs
        $output | Should -Be "-1"

        Test-Path $RobocopyDsc.workingSpaceRoboLog | Should -Be $true
        Test-Path $RobocopyDsc.workingSpaceIncreamentalArchive | Should -Be $false
        Test-Path $RobocopyDsc.workingSpaceChangeList | Should -Be $false
    }
}

Describe "robocopyjob" {
    It "does something useful" {
        $src1 = "TestDrive:\repo"
        New-Item -ItemType Directory -Path $src1
        $testfile = Join-Path -Path $src1 -ChildPath "abc.txt"
        1 | Out-File $testfile
        New-Item -ItemType Directory -Path $src1 -Force
        Test-Path $testfile | Should -Be $true
        Get-Content $testfile | Should -Be "1"
        Test-Path $src1 | Should -Be $true
    }
}
