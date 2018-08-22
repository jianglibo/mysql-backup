function Start-Process-Active
{
    param
    (
        # [System.Management.Automation.Runspaces.PSSession]$Session,
        [string]$Executable,
        [string]$Argument = "",
        [string]$WorkingDirectory,
        [string]$UserID,
        [switch]$Verbose = $false
    )

    # if (($null -eq $Session) -or ($Session.Availability -ne [System.Management.Automation.Runspaces.RunspaceAvailability]::Available))
    # {
    #     $Session.Availability
    #     throw [System.Exception] "Session is not availabile"
    # }
# -Session $Session 
    Invoke-Command -ArgumentList $Executable,$Argument,$WorkingDirectory,$UserID -ScriptBlock {
        param($Executable, $Argument = "", $WorkingDirectory, $UserID)
        $action = New-ScheduledTaskAction -Execute $Executable -Argument $Argument -WorkingDirectory $WorkingDirectory
        $principal = New-ScheduledTaskPrincipal -userid $UserID
        $task = New-ScheduledTask -Action $action -Principal $principal
        $task.State | Out-Host
        (-not $task) -and (Write-Error "error ...")
        $taskname = "_StartProcessActiveTask"
        try 
        {
            $registeredTask = Get-ScheduledTask $taskname -ErrorAction SilentlyContinue
        } 
        catch 
        {
            $registeredTask = $null
        }
        if ($registeredTask)
        {
            Unregister-ScheduledTask -InputObject $registeredTask -Confirm:$false
        }
        $registeredTask = Register-ScheduledTask $taskname -InputObject $task

        Start-ScheduledTask -InputObject $registeredTask

        Unregister-ScheduledTask -InputObject $registeredTask -Confirm:$false
    }
}

# $password = get-content C:\cred.txt | convertto-securestring
# $credentials = new-object -typename System.Management.Automation.PSCredential -argumentlist "172.19.253.244\Administrator",$password
# $session = New-PSSession -ComputerName 172.19.253.244 -Credential $credentials

Start-Process-Active  -Executable "calc.exe" -WorkingDirectory . -UserID Admin -Argument " "

# ($session -and $session.id ) -and (Remove-PSSession -Id $session.Id) | Out-Null