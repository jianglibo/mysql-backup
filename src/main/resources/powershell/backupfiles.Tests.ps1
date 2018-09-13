$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$sut = (Split-Path -Leaf $MyInvocation.MyCommand.Path) -replace '\.Tests\.', '.'
. "$here\$sut"

Describe "backupfiles conitnuous directories" {
    $parent = "TestDrive:\folder"

    $origin = "TestDrive:\folder\src"
    New-Item -ItemType Directory -Path $origin

    "abc" | Out-File (Join-Path -Path $origin -ChildPath "kkv.txt")
    
    $d = "TestDrive:\folder\src.1"
    New-Item -ItemType Directory -Path $d

    $d = "TestDrive:\folder\src.2"
    New-Item -ItemType Directory -Path $d

    $d = "TestDrive:\folder\src.c"
    New-Item -ItemType Directory -Path $d

    $d = "TestDrive:\folder\srccc"
    New-Item -ItemType Directory -Path $d

    $d = "TestDrive:\folder\srccc\sys"
    New-Item -ItemType Directory -Path $d

    $d = "TestDrive:\folder\srccc\kka"
    New-Item -ItemType Directory -Path $d

    "abc" | Out-File -FilePath "TestDrive:\folder\src.3"

    It "should handle include" {
        # out effect. Because -Path is neither recursive nor leading to directory contents.
        # (Get-ChildItem -Path "TestDrive:\folder" -Include "src.*").Length | Should -Be 0
        # [array]$r = Get-ChildItem -Path "TestDrive:\folder\*" -Directory -Include "src.*" | Out-Host

        [array]$r = Get-ChildItem -Path "TestDrive:\folder\*"
        $r.Count | Should -Be 6

        [array]$r = Get-ChildItem -Path "TestDrive:\folder"
        $r.Count | Should -Be 6


        [array]$r = Get-ChildItem -Path "TestDrive:\folder\src.*"
        $r.Count | Should -Be 4

        $r = $r | Where-Object Name -Match ".*\.\d+$"
        $r.Count | Should -Be 3

        $r = $r | Where-Object Name -Match ".*\.\d+$" | Where-Object {$_ -is [System.IO.DirectoryInfo]}
        $r.Count | Should -Be 2

        [array]$r = Get-ChildItem -Path "TestDrive:\folder\src*"
        $r.Count | Should -Be 6

        # in effect. Because -Path is leading to directory contents.
        # -Include  only include leaf object.
        [array]$r = Get-ChildItem -Path "TestDrive:\folder\*" -Include "src*"
        $r.Count | Should -Be 1

    }

    It "should get next int 3." {
        [System.IO.DirectoryInfo[]]$r = Get-ChildItem -Path "${origin}.*" | 
            Where-Object Name -Match ".*\.\d+$" |
            Where-Object {$_ -is [System.IO.DirectoryInfo]}
        $r.Count | Should -Be 2

        $r[0].Name | Should -Be "src.1"
        $r[1].Name | Should -Be "src.2"
        $r = $r | Sort-Object -Property Name -Descending
        $r[0].Name | Should -Be "src.2"
        $r[1].Name | Should -Be "src.1"
        $maxNameIdx = $r | Select-Object -First 1 -ExpandProperty Name |
             ForEach-Object {if ($_ -match ".*\.(\d+)$") {$Matches[1]}}
        
        $maxNameIdx | Should -BeExactly 2
        # string first.
        $nextIdx = $maxNameIdx + 1;
        $nextIdx | Should -Be 21
        # int fist
        $nextIdx = 1 + $maxNameIdx;
        $nextIdx | Should -Be 3

        $tf = Get-ChildItem -Path "${origin}.*" | 
            Where-Object Name -Match ".*\.\d+$" |
            # Where-Object {$_ -is [System.IO.DirectoryInfo]} |
            # We can not handle this situation, mixed files and directories.
            Select-Object -Last 1 -ExpandProperty Name |
            ForEach-Object {if ($_ -match ".*\.(\d+)$") {$origin + "." + (1 + $Matches[1])}} |
            ForEach-Object {Copy-Item -Path $origin -Recurse -Destination $_; $_}
        $created = "TestDrive:\folder\src.4"
        $tf |Should -Be $created
        Test-Path -Path $tf | Should -Be $true

        $kkvpath = Join-Path -Path $created -ChildPath "kkv.txt" 

        Test-Path $kkvpath | Should -Be $true

        Get-Content -Path $kkvpath | Should -Be "abc"

    }

    It "should visible in all pipelines" {
        $r = $a =1;$a | ForEach-Object {$_}
        $r | Should -Be 1

        $r = $a =1;$a | ForEach-Object {$a}
        $r | Should -Be 1

        $r = 1,2,3 | ForEach-Object -Begin {$s=0} -Process {$s+=$_;$_} -End {$s} | ForEach-Object {$_ + $s}

        $r | Should -Be @(2,5,9,12)
    }

    It "Should work as expected." {
        [int]$r = $true -and 5
        # boolean cast to int, $true results 1, $false results 0
        $r |Should -Be 1
        [int]$false | Should -Be 0

        $n = $null | ForEach-Object {$_}
        $n | Should -Be $null

        # $null doesn't be filter outed.
        [array]$n = $null, 5 | ForEach-Object {$_}
        $n.Count | Should -Be 2
        $n | Should -Be @($null, 5)

        [array]$n = $null, 5 | Where-Object {$_}
        $n.Count | Should -Be 1
        $n | Should -Be @(5)
    }

    It "should found my.ini in effective." {
        $r = " 1 2 3 4 ", "5" |
         Where-Object {($_.trim() -split "\s+").Count -gt 3 } 
        $r |Should -Be " 1 2 3 4 "

        # e:\wamp64\bin\mysql\mysql5.7.21\bin\mysql.exe --help --verbose |
        #  ForEach-Object -Begin {$mt = $false} -Process { if($_ -match "Default options are read from") {$mt = $true}}
        #  Out-Host

        $r = e:\wamp64\bin\mysql\mysql5.7.21\bin\mysql.exe --help --verbose |
         ForEach-Object {$_.trim()} |
         Where-Object {$_ -match '.*\.(cnf|ini)$'} |
         Select-Object -First 1 |
         ForEach-Object {$_ -split '\s+'} |
         Where-Object {Test-Path -Path $_ -PathType Leaf} |
         Select-Object -First 1 # | Out-Host

         $r | Should -BeTrue
    }
    
    It "should parse mysql output" {
        $vnames = "innodb_version", "protocol_version", "version", "version_comment", "version_compile_machine", "version_compile_os"

        E:\wamp64\bin\mysql\mysql5.7.21\bin\mysql.exe -uroot -p123456 -e "show variables"  |
        Where-Object {$_} |
        Where-Object {$vnames -contains ($_ -split "\s+")[0] } |
        Out-Host
    }
}
