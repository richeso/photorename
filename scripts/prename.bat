echo off
set glasspath=%classpath%
set classpath=..\build\libs\photorename.jar;%classpath%;
java PhotoRenamer %1 %2 %3 %4 %5
set classpath=%glasspath%