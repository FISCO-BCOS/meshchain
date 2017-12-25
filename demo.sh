#!/bin/sh

#注册商户Id=1,name=bcos

javaPath=`which java`
if [ ${javaPath}"" = "" ];then
    echo "java not found in PATH"
    exit
fi

merchantId="fisco-bcos"


java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract demo fisco-bcos
