echo off
REM Example call:  1st arg: input directory, 2nd arg: offset times
REM prename.bat c:\directory\to\photos 00:00
set glasspath=%classpath%
set classpath=..\build\libs\photorename.jar;%classpath%;
java PhotoRenamer %1 %2 %3 %4 %5
set classpath=%glasspath%