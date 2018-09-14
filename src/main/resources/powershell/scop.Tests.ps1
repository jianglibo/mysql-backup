$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$sut = (Split-Path -Leaf $MyInvocation.MyCommand.Path) -replace '\.Tests\.', '.'
. "$here\$sut"

Describe "scope cleared." {

    $noversion = "TestDrive:\folder\noversions\nov"
    New-Item -ItemType Directory -Path $noversion

    It "does't pass through pipes." {
        $a='c:\windows\';$a| ForEach-Object {$a} | ForEach-Object {$a + 1} | Should -Be "c:\windows\1"
    }

    It "in complex situation." {
            'TestDrive:\folder\noversions\nov' |
            ForEach-Object {$f=$_};$f + '*' |
            Get-ChildItem |
            Foreach-Object {@{base=$_;dg=[int](Select-String -InputObject $_.Name -Pattern '(\d*)$' -AllMatches).matches.groups[1].Value}} |
            Sort-Object -Property @{Expression={$_.dg};Descending=$true} |
            Select-Object -First 1 |
            ForEach-Object {$f + '.' + ($_.dg + 1)} |
            ForEach-Object { if(Test-Path $f -Type Container) {Copy-Item -Path $f -Recurse -Destination $_} else {Copy-Item -Path $f -Destination $_}; $_} |
            Should -Be "TestDrive:\folder\noversions\nov.1"
    }
}
