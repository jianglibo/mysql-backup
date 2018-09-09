Param(
    [Parameter(Mandatory = $false)]
    [ValidateSet('echo', 'exitCode', 'increamental')]
    [String]
    $action
)

class RobocopyItem {
    [ValidateNotNullOrEmpty()][string]$source
    [ValidateNotNullOrEmpty()][string]$dstRelative
}
#  $RobocopyDescription.robocopyDst -ChildPath $ht.dstRelative), $RobocopyDescription.workingSpaceRoboLog | Out-String
class RobocopyDescription {
    [ValidateNotNullOrEmpty()][string]$repo
    [ValidateNotNullOrEmpty()][string]$robocopyDst
    [ValidateNotNullOrEmpty()][string]$workingSpaceRoboLog
    [ValidateNotNullOrEmpty()][string]$workingSpaceChangeList
    [ValidateNotNullOrEmpty()][string]$compressCommandInstance
    [ValidateNotNullOrEmpty()][string]$workingSpaceIncreamentalArchive
    [ValidateNotNullOrEmpty()][string]$compressExe
    [ValidateNotNullOrEmpty()][string]$workingSpace
    [RobocopyItem[]]$robocopyItems
}

function ConvertTo-RobocopyDescription {
    Param(
        [parameter(Mandatory = $true)]
        [psobject]$json
    )
    [RobocopyItem[]]$roboItems = $json.robocopyItems | Select-Object source, dstRelative
    [RobocopyDescription]$v = @{
        repo                            = $json.repo
        robocopyDst                     = $json.robocopyDst
        workingSpaceRoboLog             = $json.workingSpaceRoboLog
        workingSpaceChangeList          = $json.workingSpaceChangeList
        compressCommandInstance         = $json.compressCommandInstance
        workingSpaceIncreamentalArchive = $json.workingSpaceIncreamentalArchive
        workingSpace                    = $json.workingSpace
        compressExe                     = $json.compressExe
        robocopyItems                   = $roboItems
    }
    $v
}

function Start-Robocopy {
    Param(
        [parameter(Mandatory = $true)]
        [RobocopyDescription]$RobocopyDsc,
        [parameter(Mandatory = $true)]
        [string[]]$robocopies
    )

    Remove-Item -Path $RobocopyDsc.workingSpaceRoboLog, $RobocopyDsc.workingSpaceIncreamentalArchive, $RobocopyDsc.workingSpaceChangeList -Force -ErrorAction SilentlyContinue
    
    $output = $robocopies | Invoke-Expression

    if ($LASTEXITCODE -gt 8) {
        $output
        $LASTEXITCODE
        return
    }
<#
     Winrar
     0     Successful operation.
     1     Non fatal error(s) occurred.
     2     A fatal error occurred.
     3     Invalid checksum. Data is damaged.
     4     Attempt to modify an archive locked by 'k' command.
     5     Write error.
     6     File open error.
     7     Wrong command line option.
     8     Not enough memory.
     9     File create error
    10     No files matching the specified mask and options were found.
    11     Wrong password.
   255     User stopped the process.
#>
    [array]$content = Get-Content $RobocopyDsc.workingSpaceRoboLog |
        ForEach-Object {$_.trim() -replace "\\", "/"} |
        Where-Object {$_ -notmatch '.*/$'} |
        Where-Object {($_ -split '\s+').Length -gt 2} |
        ForEach-Object {($_ -split '\s+')[-1]} |
        Tee-Object -FilePath $RobocopyDsc.workingSpaceChangeList
    if ($content.Length -ne 0) {
        $output = & $RobocopyDsc.compressExe a $RobocopyDsc.workingSpaceIncreamentalArchive "@$($RobocopyDsc.workingSpaceChangeList)"
        if ($LASTEXITCODE -ne 0) {
            $output
            $LASTEXITCODE
        }
        else {
            $RobocopyDsc.workingSpaceIncreamentalArchive
            0
        }
    } else {
        -1
    }
}

$robocopyDescription = $null
$robocopies = $null

# assign_line{robocopyDescription}
# assign_line{robocopies}

if ($robocopyDescription) {
    $json = $robocopyDescription | ConvertFrom-Json
    $robocopyDescription = ConvertTo-RobocopyDescription -json $json
}

if ($robocopies) {
    $robocopies = $robocopies -split ","
}

switch ($action) {
    "echo" { "echo" }
    "exitCode" {exit 10}
    "increamental" {
        Start-Robocopy $robocopyDescription $robocopies
    }
    Default {}
}

# Install-Module -Name PSWindowsUpdate -RequiredVersion 1.5.2.2
# do {
#     Get-ChildItem|foreach-object { $_;break }
# } while ($false)

# do {1..100000|ForEach-Object {if ($_ -gt 100) {break} else {$_}}| Out-Host} while ($false)

# function Robocopyjob {
#     Param(
#         [parameter(Mandatory=$true)]
#         [RobocopyDescription]$RobocopyDsc
#     )
#     # try {
#     "robocobydst", "workingspace/compressed" |
#         ForEach-Object {Join-Path -Path $RobocopyDsc.repo -ChildPath $_} |
#         Where-Object {-not (Test-Path $_)} |
#         ForEach-Object {New-Item -ItemType Directory -Path $_}


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
# }
