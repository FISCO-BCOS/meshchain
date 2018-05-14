<center> <h1>并行计算</h1> </center>

<a name="summary" id="summary"></a>
# 功能介绍
在研究和实现区块链平台和进行业务落地的过程中，我们意识到，区块链的运行速度会受多种因素影响，包括加密解密计算、交易广播和排序、共识算法多阶段提交的协作开销、虚拟机执行速度，以及包括CPU核数主频、磁盘IO、网络带宽等。由于区块链是先天的跨网络的分布式协作系统，而且强调安全性、可用性、容错性、一致性、事务性，用较复杂的算法和繁琐的多参与方协作来获得去信任化、数据不可篡改以及交易可追溯等特出的功能优势，根据分布式的CAP原理，在同等的硬件资源投入的前提下，区块链的性能往往低于中心化的系统，其表现就是并发数不高，交易时延较明显。

我们已经在多个方面对系统运行的全流程进行细致的优化，包括加密解密计算，交易处理流程，共识算法，存储优化等，使我们的区块链平台在单链架构时，运行速度达到了一个较高的性能水准，基本能满足一般的金融业务要求。

同时我们也意识到，对于用户数、交易量、存量数据较大或可能有显著增长的海量服务场景，对系统提出了更高的容量和扩展性要求，单链架构总是会遇到软件架构或硬件资源方面的瓶颈。
而区块链的系统特性决定，在区块链中增加节点，只会增强系统的容错性，增加参与者的授信背书等，而不会增加性能，这就需要通过架构上的调整来应对性能挑战，所以，我们提出了“并行计算，多链运行”的方案。

# 快速体验

如果想快速体验多链和跨链应用，请参照[quick_start.md](https://github.com/FISCO-BCOS/meshchain/blob/master/quick_start.md)

# 使用方式


### 搭建fisco-bcos：

至少部署路由链，分组链1，分组链2，分组链3。每条链的节点个数任意，至少为1。

分了简化步骤，下面配置将会是：一条路由链，三条分组链。

首先部署路由链。

fisco-bcos搭建步骤：请参照[安装说明](https://github.com/FISCO-BCOS/FISCO-BCOS)

部署fisco-bcos后，查看当前某个节点监听的rpc端口（假设当前目录在FISCO-BCOS）

```
cat config.json
```

得到该RPC端口(rpcport字段)后，开始进行部署系统合约(假设当前目录是FISCO-BCOS)：

```
cd web3lib

#替换 var proxy="http://ip:port"为节点的ip和rpc端口，保存
vim config.json

# 回到上一层目录的systemcontractv2目录
cd ../systemcontractv2
babel-node deploy.js
```

得到系统代理合约地址后，如箭头所示

![系统合约部署结果](https://github.com/FISCO-BCOS/meshchain/raw/master/images/system_contract_result.png)

路由链的所有节点，把上述得到的地址替换FISCO-BCOS目录下的config.json的systemproxyaddress字段，如图所示

![config.json](https://github.com/FISCO-BCOS/meshchain/raw/master/images/config.json.png)

然后重启路由链的所有节点

```
#找出路由链所有节点的所有rpc port. 如上图所示，单个节点的config.json的rpcport字段
#假设有路由链四个节点
netstat -npl | grep -E "port1|port2|port3|port4"
killall ${proc1} ${proc2} ${proc3} ${proc4}
```

重启后，然后部署Meshchain.sol合约（假设当前目录是FISCO-BCOS）：

```
# 回到上一层目录，然后进入tool目录（当前目录为FISCO-BCOS）
cd tool

# Meshchain.sol相关合约可从以下方式获得
git clone https://github.com/FISCO-BCOS/meshchain.git
cp meshchain/src/main/resources/*.sol ./

#注意Meshchain没有.sol结尾
babel-node deploy.js Meshchain

#首次添加Meshchain合约，执行babel-node cns_manager.js add Meshchain，否则执行babel-node cns_manager.js update Meshchain

babel-node cns_manager.js add Meshchain

#检查是否添加成功，如果输入的结果中，包含有Meshchain，则代表成功
babel-node cns_manager.js list
```

如下图：

![meshchain_deploy](https://github.com/FISCO-BCOS/meshchain/raw/master/images/meshchain_deploy.png)


以上，则代表路由链部署成功。分组链1,分组链2,分组链3也执行以上的步骤。

### 成功部署多条链后，开始部署proxy：

```
# 进入meshchain目录(假设当前目录是FISCO-BCOS/tool)
cd meshchain

#gradle安装说明，请参照https://gradle.org/install/
#java安装说明，请参照http://www.oracle.com/technetwork/java/javase/downloads/index.html
#确保PATH里面已经追加gradle，譬如PATH=$GRADLE_HOME/bin:$PATH

gradle build
cd dist/conf

#修改applicationContext.xml，bean id="routeService"的ip和端口，为路由链的所有节点的ip和channelPort。ip前面的nodeid可以任意填写，填写所有节点的ip和channelPort是为了保证容错，channelPort则为fisco-bcos节点启动时候指定的config.json里面的channelPort字段。如下图一和图二所示

vim applicationContext.xml
```

![application.xml.route](https://github.com/FISCO-BCOS/meshchain/raw/master/images/application.xml.route.jpg)


![config.json.channelPort](https://github.com/FISCO-BCOS/meshchain/raw/master/images/config.json.channelPort.png)


```
#同理，修改applicationContext.xml，bean id="set0Service"的ip和端口，为分组链0的所有节点的ip和channelPort。bean id="set1Service"的ip和端口，为分组链1的所有节点的ip和channelPort。bean id="set2Service"的ip和端口，为分组链2的所有节点的ip和channelPort

vim applicationContext.xml
```

结果如下：

![application.xml.set](https://github.com/FISCO-BCOS/meshchain/raw/master/images/application.xml.set.png)


### 更改配置文件config.xml
<span id = "config.xml"></span>

```

#修改config.xml
vim config.xml
```

```
<?xml version="1.0" encoding="UTF-8" ?>
<config>
    <privateKey>bcec428d5205abe0f0cc8a734083908d9eb8563e31f943d760786edf42ad67dd</privateKey> <!--用作发送交易做签名的私钥-->
    <routeAddress>${routeAddress}</routeAddress> <!-- 路由合约，这个在启动proxy执行start.sh脚本时候会部署路由合约并替换这个变量-->
    <hotChainName>set2Service</hotChainName> <!--热点链的名字-->
    <hotAccounts>fisco-dev</hotAccounts> <!--热点账户的名字-->
    <enableTimeTask>0</enableTimeTask> <!--是否开启定时任务 0：不开启 1：开启-->
    <timeTaskIntervalSecond>60</timeTaskIntervalSecond> <!--定时任务间隔，秒为单位-->
</config>

```


### 部署路由合约:

<span id = "deploy_route">执行命令：</span>

在start.sh的脚本中，会有这么的一段命令来部署路由合约：

```
#注意不用单独执行这行命令，sh start.sh过程中会部署
java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract deploy conf/route.json
```

<span id="route.json"></span>
route.json 格式:

```
[
    {
        "set_name":"set0Service",
        "set_warn_num":4,
        "set_max_num":5,
        "set_node_list":[]
    },
    {
        "set_name":"set1Service",
        "set_warn_num":4,
        "set_max_num":5,
        "set_node_list":[]
    },
    {
        "set_name":"set2Service",
        "set_warn_num":4,
        "set_max_num":5,
        "set_node_list":[]
    }
]
```

说明：

1. set_name是指用户链的名字，如果有多个set_name，不能重复，且必须要与applicationContext的bean id="xxxService"保持一致。

2. set_warn_num：是指某个set达到set_warn_num则会有相关的日志告警，日志设置请参考conf/log4j2.xml

3. set_max_num：是指某个set最多能容纳set_max_num个用户

4. 路由合约的默认分配规则，一个set的[uid](#uid)个数最大为set_max_num.set0满了后，再增加用户则会在set1添加，理论上无容量限制。


### 启动server监听:

启动http server:

```
sh start.sh
```

监听的是http server,就可以curl发送post http请求，如用户注册，用户充值和用户转账.

注意：conf目录下的ca.crt需要跟每条链下的所有节点的ca.crt文件保持一致。节点的ca.crt文件位置在config.json的datadir指定的目录下

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
	"data":“ok”，
	“message”:""
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

# 验证

### 查询用户的资产

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

# 热点账户

热点账户的相关说明请参考：[并行计算和热点账户的解决方案](https://github.com/FISCO-BCOS/whitepaper#333-%E5%B9%B6%E8%A1%8C%E8%AE%A1%E7%AE%97%E5%92%8C%E7%83%AD%E7%82%B9%E8%B4%A6%E6%88%B7%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88)

在conf/config.xml中，假如我们指定了set2Service为热点链。那么分组链set0Service，ser1Service会有热点账户的影子户。举例，热点账户fisco-dev在热点链set2Service注册了，identity为1。那么在set0Service，ser1Service上面，必须要有用户名为fisco-dev，identity为0的‘影子户’。


因此，倘若普通用户A给热点账户H转账，实现方式是A会找到热点账户在A用户所在分组链的同名影子户H'，然后给影子户H'转账，这时候不涉及到跨链的操作。之后，影子户与热点户的转账就是跨链的操作，该操作是由一个异步的线程去达到的。

修改conf/config.xml

```
<enableTimeTask>1</enableTimeTask> <!--0:不开启 1:开启-->
<hotAccounts>fisco-dev</hotAccounts> <!--确保该账户为热点账户,且在链上已注册,逗号分隔多个-->
<hotChainName>set2Service</hotChainName>
<timeTaskIntervalSecond>30</timeTaskIntervalSecond>
```

配置生效后，重启服务

```
sh stop.sh && sh start.sh
```

检查日志

```
grep 'RelayTask start' $log
```

$log是指conf/log4j2.xml里面指定的文件名字。当出现这样的日志关键字，可以配合工具-查询用户的资产来验证跨链操作是否生效。





