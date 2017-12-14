#!/bin/sh

if [ ! -f "conf/config.xml" ];then
    echo "not found conf/config.xml file"
    exit
fi

routeAddress=`grep '${routeAddress}' conf/config.xml`

if [ ${routeAddress}"" != "" ];then
    #发布路由合约
    if [ ! -f "conf/route.json" ];then
        echo "not found conf/route.json file"
        exit
    fi
    javaPath=`which java`
    if [ ${javaPath}"" = "" ];then
        echo "java not found in PATH"
        exit
    fi

    java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract deploy conf/route.json > .route.address

    address=`awk -F':' '{print $2}' .route.address`
    if [ ${address}"" = "" ];then
        rm .route.address
        echo "deploy route contract failed."
        exit
    fi

    rm .route.address

    sed -i 's/${routeAddress}/'${address}'/g' conf/config.xml
fi

existed=`netstat -npl | grep 8081 | grep java | wc -l`
if [ ${existed}"" = "0" ];then
    javaPath=`which java`
    if [ ${javaPath}"" = "" ];then
        echo "java not found in PATH"
        exit
    fi
    setsid java -cp conf/:apps/*:lib/* -Dserver=http -Dport=8081 org.bcos.proxy.main.Start >> /dev/null 2>&1 &
else
    echo "java process is running..."
fi