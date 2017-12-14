#!/bin/sh

#注册商户Id=1,name=bcos

javaPath=`which java`
if [ ${javaPath}"" = "" ];then
    echo "java not found in PATH"
    exit
fi

echo "请热点账户Id(不可重复注册,Id是32字节的字符串，可以是1,2等等):"
read merchantId

echo "start to register merchant.merchantId=${merchantId},name=bcos"
java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract registerMerchant ${merchantId} bcos > .log

fileCount=`wc -l .log`
count=`grep 'failed' .log | wc -l`

if [ ${count}"" = "0" ];then
    echo "register merchant success.merchantId=${merchantId},name=bcos"
    while true
    do
        echo "start to user deposit.uid=1.amount=1000.return:"
        curl 'http://127.0.0.1:8081' -d  '{"method":"userDeposit","uid":"1","version":"","contractName":"Meshchain","params":["1000"]}'
        echo "start to user consume.uid=1.merchantId=${merchantId},amount=1000.return:"
        curl 'http://127.0.0.1:8081' -d  '{"method":"consume","uid":"1","version":"","contractName":"Meshchain","params":["'${merchantId}'",1000]}'

        echo "do it again:Y/N"
        read action

        if [ ${action}"" = "N" -o ${action}"" = "n" ];then
            break
        elif [ ${action}"" = "Y" -o ${action}"" = "y" ];then
            continue
        else
            echo "not support command.exit"
            exit
        fi

    done
else
    echo "register merchant failed.check the error log"
    exit
fi

