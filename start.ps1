param (
    [switch]$Debug
)

$here =  $MyInvocation.MyCommand.Path | Split-Path -Parent

$db = $here | Join-Path -ChildPath 'dbdata' | Join-Path -ChildPath 'db'


# In addition to application.properties files, profile-specific properties can also be defined by using the following naming convention: application-{profile}.properties. The Environment has a set of default profiles (by default, [default]) that are used if no active profiles are set. In other words, if no profiles are explicitly activated, then properties from application-default.properties are loaded.
# Profile-specific properties are loaded from the same locations as standard application.properties, with profile-specific files always overriding the non-specific ones, whether or not the profile-specific files are inside or outside your packaged jar.
# If several profiles are specified, a last-wins strategy applies. For example, profiles specified by the spring.profiles.active property are added after those configured through the SpringApplication API and therefore take precedence.
# [Note]
# If you have specified any files in spring.config.location, profile-specific variants of those files are not considered. Use directories in spring.config.location if you want to also use profile-specific properties.

$SpringParams = '--spring.config.location=classpath:/application.properties,file:./application.properties'

# :: IF DEFINED upgrade-jar SET _db=%wdirslash%dbdata.prev/db
# :: SET springParams=%springParams% ----spring.profiles.active=prod
$SpringParams = "$SpringParams --spring.datasource.url=`"jdbc:hsqldb:file:${db};shutdown=true`""

$jar = Get-ChildItem -Recurse -Include "mysql-backup-*.jar" | Sort-Object -Property FullName -Descending | Select-Object -First 1 | Select-Object -ExpandProperty FullName

$cmd = if ($Debug) {
	"java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y -jar $jar $SpringParams"
} else {
	"java -jar $jar $SpringParams"
}

"start executing command: $cmd" | Out-Host

Invoke-Expression -Command $cmd

