ECHO Build jar file

CALL ./gradlew server:dist

ECHO Copy jar file to mods folder

XCOPY /y "D:\Project\MindustryServer\server\build\libs\server-release.jar" "D:\Project\MindustryTool\MindustryToolBackend\mindustry"
