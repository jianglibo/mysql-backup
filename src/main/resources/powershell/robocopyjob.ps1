Param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('echo','excel','powerpoint')]
    [String]
    $action
)
function Robocopyjob {
    Param(
       [parameter(Mandatory=$true, ValueFromPipeline=$true)]
       [String]
       $Repopath
        ,[parameter(Mandatory=$true)]
        [hashtable[]]$dirpair
    )

    try {
        
        "robocobydst", "compressed", "log" | ForEach-Object {Join-Path -Path $Repopath -ChildPath $_} | Where-Object {-not (Test-Path $_)} | ForEach-Object {New-Item -ItemType Directory -Path $_}

        $lock_file = New-Item -Path (Join-Path $Repopath -ChildPath "lock.lck") -ItemType File
        if (-not $lock_file) {
            return;
        }

        $number_file = Join-Path $Repopath -ChildPath number.txt

        if (-Not (Test-Path $number_file -PathType Leaf)) { 0 | Out-File -FilePath $number_file  }

        # $next_number =  Get-Content $number_file | Select-Object -First  1| ForEach-Object {[int]$_ + 1}|Tee-Object -FilePath $number_file

        # Robocopy.exe C:\Users\ADMINI~1\AppData\Local\Temp\srca repo/robocopydst/abc *.* /log+:repo/log/robocopy.log.{0} /e /fp /njh /njs
        # Robocopy.exe C:\Users\ADMINI~1\AppData\Local\Temp\srcb repo/robocopydst/abc1 *.* /log+:repo/log/robocopy.log.{0} /e /fp /njh /njs
        # Invoke-Expression -Command ""

    } finally {
        if ($lock_file) {
            Remove-Item -Path $lock_file -Force
        }
    }

}

function RobocopyExecutables {
    Param(
       [parameter(Mandatory=$true, ValueFromPipeline=$true)]
       [String]
       $Repopath
        ,[parameter(Mandatory=$true)]
        [hashtable[]]$dirpair
    )
    foreach ($ht in $dirpair) {
        "Robocopy.exe {0} ${repo}/robocoppydst/{1}" -f $ht.src, $ht.dst
    }
}

# replace block.

# do {
#     Get-ChildItem|foreach-object { $_;break }
# } while ($false)

# do {1..100000|ForEach-Object {if ($_ -gt 100) {break} else {$_}}| Out-Host} while ($false)

switch ($action) {
    "echo" { "echo" }
    Default {}
}

# Install-Module -Name PSWindowsUpdate -RequiredVersion 1.5.2.2