#!/bin/sh

pid=`netstat -npl | grep 8081 | grep -o '[0-9]\+/java' | awk -F "/" '{print $1}'`
if [ ${pid}"" = "" ];then
    echo "java process is not running"
    exit
fi 

kill ${pid}
if [ $? = 0 ];then
	echo "stop success"
else
	echo "stop failed"
fi
