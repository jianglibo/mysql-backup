Param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('echo','excel','powerpoint')]
    [String]
    $action
)

class RobocopyItem {
    [ValidateNotNullOrEmpty()][string]$source
    [ValidateNotNullOrEmpty()][string]$dstRelative
}
#  $RobocopyDescription.robocopyDst -ChildPath $ht.dstRelative), $RobocopyDescription.workingSpaceRoboLog | Out-String
class RobocopyDescription
{
    [ValidateNotNullOrEmpty()][string]$repo
    [ValidateNotNullOrEmpty()][string]$robocopyDst
    [ValidateNotNullOrEmpty()][string]$workingSpaceRoboLog
    [ValidateNotNullOrEmpty()][string]$workingSpaceChangeList
    [ValidateNotNullOrEmpty()][string]$workingSpace
    [RobocopyItem[]]$robocopyItems
}

function convertTo-RobocopyDescription {
    Param(
        [parameter(Mandatory=$true)]
        [psobject]$json
    )
    $roboItems = $json.robocopyItems | Select-Object source, dstRelative
    [RobocopyDescription]@{
        repo = $json.repo
        robocopyDst = $json.robocopyDst
        workingSpaceRoboLog = $json.workingSpaceRoboLog
        workingSpaceChangeList = $json.workingSpaceChangeList
        workingSpace = $json.workingSpace
        robocopyItems = $roboItems
    }
}

function run-robocopy {
    Param(
        [parameter(Mandatory=$true)]
        [RobocopyDescription]$RobocopyDsc,
        [parameter(Mandatory=$true)]
        [string[]]$robocopies
    )

    Remove-Item -Path $RobocopyDsc.workingSpaceRoboLog -Force
    $robocopies | Invoke-Expression

    $content = Get-Content $RobocopyDsc.workingSpaceRoboLog
    $changedFiles = $content |
         ForEach-Object {$_.trim() -replace "\\", "/"} |
         Where-Object {$_ -notmatch '.*/$'} |
         Where-Object {($_ -split  '\s+').Length -gt 2} |
         ForEach-Object {($_ -split '\s+')[-1]}
    $changedlist = Join-Path -Path $RobocopyDsc.workingSpaceRoboLog
}

function Robocopyjob {
    Param(
        [parameter(Mandatory=$true)]
        [RobocopyDescription]$RobocopyDsc
    )
    # try {
    "robocobydst", "workingspace/compressed" |
        ForEach-Object {Join-Path -Path $RobocopyDsc.repo -ChildPath $_} |
        Where-Object {-not (Test-Path $_)} |
        ForEach-Object {New-Item -ItemType Directory -Path $_}


        # $lock_file = New-Item -Path (Join-Path $Repopath -ChildPath "lock.lck") -ItemType File
        # if (-not $lock_file) {
        #     return;
        # }

        # $number_file = Join-Path $Repopath -ChildPath number.txt

        # if (-Not (Test-Path $number_file -PathType Leaf)) { 0 | Out-File -FilePath $number_file  }

        # $next_number =  Get-Content $number_file | Select-Object -First  1| ForEach-Object {[int]$_ + 1}|Tee-Object -FilePath $number_file

        # Robocopy.exe C:\Users\ADMINI~1\AppData\Local\Temp\srca repo/robocopydst/abc *.* /log+:repo/log/robocopy.log.{0} /e /fp /njh /njs
        # Robocopy.exe C:\Users\ADMINI~1\AppData\Local\Temp\srcb repo/robocopydst/abc1 *.* /log+:repo/log/robocopy.log.{0} /e /fp /njh /njs
        # Invoke-Expression -Command ""
    # } finally {
    #     if ($lock_file) {
    #         Remove-Item -Path $lock_file -Force
    #     }
    # }
}

function RobocopyExecutables {
    Param(
        [parameter(Mandatory=$true, Position = 1)]
        [RobocopyDescription]$RobocopyDescription
    )
    foreach ($ht in $RobocopyDescription.robocopyItems) {
        $v = "Robocopy.exe {0} {1} /e /fp /njh /njs /log+:{2}" -f $ht.source, (Join-Path -Path $RobocopyDescription.robocopyDst -ChildPath $ht.dstRelative), $RobocopyDescription.workingSpaceRoboLog | Out-String
        $v.Trim()
    }
}

# assign_line{robocopyDescription}

# assign_line{robocopies}

# do {
#     Get-ChildItem|foreach-object { $_;break }
# } while ($false)

# do {1..100000|ForEach-Object {if ($_ -gt 100) {break} else {$_}}| Out-Host} while ($false)

switch ($action) {
    "echo" { "echo" }
    Default {}
}

# Install-Module -Name PSWindowsUpdate -RequiredVersion 1.5.2.2