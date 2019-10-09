#!/bin/sh

ethName="fisco-bcos"
function deployContractAndStart()
{

    if [ ! -d ./systemcontract ];then
        echo "目录 ./systemcontract 不存在"
        exit
    fi

    if [ ! -d ./tool ];then
        echo "dir ./tool not esxist"
        exit
    fi

	if [ ! -d ./web3lib ];then
        echo "dir ./web3lib not exists"
        exit
    fi
    for node in `ls | grep "node_"`
    do
        #start eth
		cd $node
		chmod a+x fisco-solc
		dir=`pwd`
		export PATH=${PATH}:${dir}
		port=`grep "\"rpcport\":\"[0-9]\+\"" config.json | grep -o "[0-9]\+"`
		if [ ${port}"" = "" ];then
			echo "not found rpcport in ${node}/config.json"
			exit
		fi

		pid=`lsof -i :${port} | grep "${port}" | awk '{print $2}'`
		if [ ${pid}"" != "" ];then
			echo "${ethName} is running.pid:${pid},kill it and start."
			kill -9 ${pid}
			sleep 3 #for release resources
		fi

		sh -c "setsid ./${ethName} --config config.json --genesis genesis.json > .stdout 2>&1 &"
		sleep 3 #for init
		cd ..
    done

    ip=`grep "listenip" node_1/config.json | grep -o "[0-9]\+.[0-9]\+.[0-9]\+.[0-9]\+"`
    port=`grep "rpcport" node_1/config.json | grep -o "[0-9]\+"`

    if [ ${ip}"" = "" ];then
        echo "node_1/config.json listenip not match"
        exit
    fi

    if [ ${port}"" = "" ];then
        echo "node_1/config.json rpcport not match"
        exit
    fi

    sed -i "s/127.0.0.1:[0-9]\+/${ip}:${port}/g" ./web3lib/config.js

    babelNode=`which babel-node`
    if [ ${babelNode}"" = "" ];then
        echo "babel-node not in PATH."
        exit
    fi

    cd systemcontract
    if [ ! -d ./node_modules ];then
		npm=`which npm`
		if [ ${npm}"" = "" ];then
			echo "npm not found in PATH"
			exit
		fi

		npm install
    fi

	if [ ! -d ../web3lib/node_modules ];then
		currentDir=`pwd`
		ln -s ${currentDir}/node_modules ../web3lib/node_modules
	fi

    babel-node deploy.js > .contract.address

    if [ ! $? = 0 ];then
        echo "deploy system contract failed"
        exit
    fi

    systemContractAddress=`grep "SystemProxy address :" .contract.address | awk -F ":" '{print $2}'`
    if [ ${systemContractAddress}"" = "" ];then
        echo "get system address failed"
        exit
    fi

    rm .contract.address

    cd ..
    
    for node in `ls | grep "node_"`
    do
        cd $node
        sysOld='"systemproxyaddress":"0x0"'
        sysNew="\"systemproxyaddress\":\""${systemContractAddress}"\""
        sed -i "s/${sysOld}/${sysNew}/g" config.json

        match=`grep ${sysOld} config.json | wc -l`
        if [ !${match} = 0 ];then
            echo "replace system address failed."
            exit
        fi

        port=`grep "\"rpcport\":\"[0-9]\+\"" config.json | grep -o "[0-9]\+"`
        if [ ${port}"" = "" ];then
            echo "not found rpcport in ${node}"
            exit
        fi
        pid=`lsof -i :${port} | grep "${port}" | awk '{print $2}'`
        if [ ${pid}"" != "" ];then
            echo "start to kill ${ethName} pid:${pid} and start"
            kill -9 ${pid}
	    sleep 3 #for release resources
        fi

        sh -c "setsid ./${ethName} --config config.json --genesis genesis.json > .stdout 2>&1 &"
        cd ..
    done

    #wait for eth init
    sleep 3

    cd tool

    if [ ! -d ./node_modules ];then
    cp ../systemcontract/node_modules ./ -R
    fi

    #cp ../systemcontract/config.js ./
    echo "deploy Meshchain.sol start..."
    babel-node deploy.js Meshchain

    if [ ! $? = 0 ];then
        echo "deploy Meshchain.sol contract failed"
        exit
    fi

    babel-node cns_manager.js list > .abilist
    if [ -f .abilist ];then
        ifDeploy=`grep 'Meshchain' .abilist | wc -l`
        if [ ${ifDeploy} = "0" ];then
            babel-node cns_manager.js add Meshchain
        else
            babel-node cns_manager.js update Meshchain
        fi

        rm .abilist
        echo "deploy Meshchain.sol success..."
    else
        echo "not found .abilist file"
        exit
    fi
}

deployContractAndStart
