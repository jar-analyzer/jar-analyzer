@echo off

call mvn clean

cd agent
call mvn clean
cd ..

cd class-searcher
call mvn clean
cd ..

cd jar-analyzer-rasp
call mvn clean
cd ..

echo clean finish
