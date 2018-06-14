# 快捷部署多链

## 部署单机N链
步骤一：

```
git clone https://github.com/FISCO-BCOS/FISCO-BCOS.git
cd  FISCO-BCOS;sh build.sh
cd sample
git clone https://github.com/FISCO-BCOS/meshchain.git
cp meshchain/script/* ./ && cp meshchain/src/main/resources/*.sol ../tool
sh init_meshchain.sh 4 2 127.0.0.1 127.0.0.1 127.0.0.1 127.0.0.1
```

步骤二：

```
#启动路由链，执行过程需要较长时间，请耐心等待
cd 127.0.0.1_route/
sh start_meshchain.sh
#当看到如下图的信息，输入Y，代表deploy success
```


![meshchain_deploy](https://github.com/FISCO-BCOS/meshchain/raw/master/images/meshchain_deploy.png)

```
#分组链0
cd 127.0.0.1_set0/
sh start_meshchain.sh

#分组链1
cd 127.0.0.1_set1/
sh start_meshchain.sh


#分组链2
cd 127.0.0.1_set2/
sh start_meshchain.sh

#切换回到sample目录
cd ..

```


步骤三：

```
cd meshchain

#gradle安装说明，请参照https://gradle.org/install/
#java安装说明，请参照http://www.oracle.com/technetwork/java/javase/downloads/index.html
#确保PATH里面已经追加gradle，java，譬如PATH=$JAVA_HOME/bin:$GRADLE_HOME/bin:$PATH

gradle build
cd dist

#拷贝步骤一生成的proxyConfig.tar.gz到当前目录
mv ../../proxyConfig.tar.gz ./ && tar -zxvf proxyConfig.tar.gz && cp proxyConfig/* conf/ && rm -rf proxyConfig*

#按需修改conf/log4j2.xml的日志路径，默认/tmp/proxy.log

#启动http server 默认监听8081端口
sh start.sh

#由于初始化需要一段时间，请耐心等待若干秒，然后用命令netstat -npl | grep 8081 来查看进程是否已经监听

netstat -npl | grep 8081

```

步骤四：发送交易请求

注册协议:

```
curl http://127.0.0.1:8081 -d '{"func":"register","uid":"1","version":"","contractName":"Meshchain","params":[100,0, "fisco-dev"]}'
```

参数说明：

1. func：Meshchain合约中的某个方法，这里代表用户注册
2. uid：用作路由id，区分哪条分组链的用户
3. version：合约版本
4. contractName：合约名字
5. params:参数数组，这里第一个参数表示用户初始金额，类型uint256;第二个参数是身份类型 0:普通用户 1:热点账户 2:影子账户;第三个参数是用户名字。

response响应:

```
{
	"code":0,
	"data":“”，
	“message”:"ok"
}
```

充值协议:

```
curl http://ip:port -d '{"func":"deposit","uid":"1","version":"","contractName":"Meshchain","params":[200]}'
```

参数说明：

1. func：Meshchain合约中的某个方法，这里代表用户充值
2. uid：用作路由id，区分哪个用户
3. version：合约版本
4. contractName：合约名字
5. params:数组，这里第一个参数是金额，类型是uint256

response响应:

```
{
	"code":0,
	"data":“{\"deposit_id\":1}”，
	“message”:""
}
```

转账协议：

```
curl http://ip:port -d '{"func":"transfer","uid":"1","version":"","contractName":"Meshchain","params":["2", 10]}'
```

参数说明：

1. func：Meshchain合约中的某个方法，这里代表用户"1"给用户"2"转账
2. uid：用作路由id，区分哪个用户
3. version：合约版本
4. contractName：合约名字
5. params:数组，这里第一个参数是另外一个用户id"2"，类型是bytes32;第二个参数是金额，类型是uint256

response响应:

```
{
	"code":0,
	"data":“{\"from_transfer_id\":1, \"to_transfer_id\":1}”
	“message”:""
}
```



code的说明:

```
0:成功
10000:热点账户已存在
10001:用户已存在
10002:用户状态不正常
10003:用户不存在
10004:热点账户不存在
10005:热点账户状态不正常
10006:用户余额不足
10007:冻结余额不合法
10008:热点账户余额为0
10009:没有可释放的金额
10010:非热点账户
10011:非影子户
10012:trie proof验证失败
10013:影子户不存在
10014:影子户状态不正常
10015:影子户已存在
10016:公钥列表不存在
10017:验证签名失败
10018:金额非法
10019:交易不存在
10020:热点账户已存在
```


查询用户的资产

```
cd meshchain/dist
java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract queryUserInfo ${uid} 
```

uid是指用户唯一的uid，如上述的“1”

response响应如下：

```
uid:1, queryUserInfo get availAssets:290 unAvailAssets: 0, identity:1
```

如果觉得当前某条分组链的容量需要扩大，那么可以执行以下的命令


分组链扩容

```
cd meshchain/dist
# 参数‘0 3 3’是对setid=0的容量设置 warnNum=3 maxNum=3
java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract expandSet 0 3 3
```
response响应如下：

```
expandSet success.
```

查询某个分组链的容量信息

```
#参数‘0’代表查询的是setid=0的容量信息
java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract getSetCapacity 0
```

response响应如下：

```
warn num:3, max num:3
```
## 多机部署多链

步骤一：

```
git clone https://github.com/FISCO-BCOS/FISCO-BCOS.git
cd  FISCO-BCOS;sh build.sh
cd sample
git clone https://github.com/FISCO-BCOS/meshchain.git
cp meshchain/script/* ./ && cp meshchain/src/main/resources/*.sol ../tool
 
#其中，链数目>=4 1<=节点数目<=4 后面的ip是表示，ip0部署链0，ip1部署链1等等。默认情况下，链0部署路由链，其他链部署分组链，也称为set链。
sh init_meshchain.sh <链的数目> <每条链的节点数目> <ip0> <ip1>...
```

步骤二：

步骤一生成每个ip的tar.gz压缩包，需要在对应的机器ip上面部署，部署的目录可以任意。压缩包会是xxx.xxx.xxx.xxx.tar.gz和proxyConfig.tar.gz

每个xxx.xxx.xxx.xxx.tar.gz，通过scp或者其他方式传输eth包到对应机器任一目录，执行：

```
tar -zxvf xxx.xxx.xxx.xxx.tar.gz
cd xxx.xxx.xxx.xxx

#最后输出deploy Meshchain.sol success...代表成功
sh start_meshchain.sh

```

步骤三：

步骤一生成的proxyConfig.tar.gz中的文件需要拷贝到部署[proxy](https://github.com/FISCO-BCOS/meshchain.git) conf/下面的对应文件，执行

```
cd meshchain

#gradle安装说明，请参照https://gradle.org/install/
#java安装说明，请参照http://www.oracle.com/technetwork/java/javase/downloads/index.html
#确保PATH里面已经追加gradle，java，譬如PATH=$JAVA_HOME/bin:$GRADLE_HOME/bin:$PATH

gradle build
cd dist

#拷贝步骤一生成的proxyConfig.tar.gz到当前目录
tar -zxvf proxyConfig.tar.gz && cp proxyConfig/* conf/ && rm -rf proxyConfig

#按需修改conf/log4j2.xml的日志路径，默认/tmp/proxy.log

#启动http server 默认监听8081端口
sh start.sh

#由于初始化需要一段时间，请耐心等待若干秒，然后用命令netstat -npl | grep 8081 来查看进程是否已经监听

netstat -npl | grep 8081
```

步骤四：发送交易操作 

如上协议
