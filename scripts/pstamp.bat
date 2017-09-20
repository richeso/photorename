echo off
set glasspath=%classpath%
set classpath=..\build\libs\photorename.jar;%classpath%;
java PhotoStamp %1 %2
set classpath=%glasspath%
