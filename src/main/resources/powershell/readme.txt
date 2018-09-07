#Install-Module -Name Pester -Force -SkipPublisherCheck
# new-fixture
invoke-Pester

Param(
    [parameter(Mandatory=$true, ValueFromPipeline=$true)]
    [String] $Repopath
)

$Repopath | Out-Host