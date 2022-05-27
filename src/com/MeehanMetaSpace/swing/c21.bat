@echo off
if "%1"=="" goto usage

copy %1 \facsxpert\src\com\MeehanMetaSpace\swing
goto end
:noway
echo 
echo 
echo No way ... must change by hand 'cos 2.1 changes this!
echo 
echo 
goto end
:usage
echo 
echo 
echo Usage %0 {file to copy}
echo 
echo 
:end

