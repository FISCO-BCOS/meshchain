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

至少部署配置链，热点链，用户链1，用户链2。每条链的节点个数任意，至少为1。

首先部署路由链。

fisco-bcos搭建步骤：请参照[安装说明](https://github.com/FISCO-BCOS/FISCO-BCOS)

部署fisco-bcos后，查看当前某个节点监听的rpc端口（假设当前目录在FISCO-BCOS）

```
cat config.json
```

得到该RPC端口后，开始进行部署系统合约(假设当前目录是FISCO-BCOS)：

```
cd systemcontractv2

#替换 var proxy="http://ip:port"为节点的ip和rpc端口，保存
vim config.json

babel-node deploy.js
```

得到系统代理合约地址后，如箭头所示

![系统合约部署结果](https://github.com/FISCO-BCOS/meshchain/raw/master/images/system_contract_result.png)

路由链的所有节点，把上述得到的地址替换FISCO-BCOS目录下的config.json的systemproxyaddress字段，如图所示

![config.json](https://github.com/FISCO-BCOS/meshchain/raw/master/images/config.json.png)

然后重启路由链的所有节点

```
killall fisco-bcos
```

重启后，然后部署Meshchain.sol合约（假设当前目录是FISCO-BCOS）：

```
cd tool

#替换 var proxy="http://ip:port"为路由链中某个节点的ip和rpc端口，保存
vim config.json

#注意Meshchain没有.sol结尾
babel-node deploy.js Meshchain

#首次添加Meshchain合约，执行babel-node abi_name_service_tool.js add Meshchain，否则执行babel-node abi_name_service_tool.js update Meshchain

babel-node abi_name_service_tool.js add Meshchain

#检查是否添加成功，如果输入的结果中，包含有Meshchain，则代表成功
babel-node abi_name_service_tool.js list
```

如下图：

![meshchain_deploy](https://github.com/FISCO-BCOS/meshchain/raw/master/images/meshchain_deploy.png)


以上，则代表路由链部署成功。热点链，用户链也执行以上的步骤。

### 成功部署多条链后，开始部署relay：

```
git clone https://github.com/FISCO-BCOS/meshchain.git

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
#同理，修改applicationContext.xml，bean id="hotService"的ip和端口，为热点链的所有节点的ip和channelPort

vim applicationContext.xml

#假设目前只有两条用户链set0，set1，那么追加以下内容到applicationContext.xml,ip和channelPort是用户链的节点ip和channelPort

<bean id="set0Service" class="org.bcos.channel.client.Service">
	<property name="orgID" value="WB" />
	<property name="allChannelConnections">
		<map>
		<entry key="WB">
			<bean class="org.bcos.channel.handler.ChannelConnections">
				<property name="connectionsStr">
					<list>
						<value>node1@ip:channelPort</value>
						<value>node2@ip:channelPort</value>
						<value>node3@ip:channelPort</value>
						<value>node4@ip:channelPort</value>
					</list>
				</property>
			</bean>
		</entry>
		</map>
	</property>
</bean>

<bean id="set1Service" class="org.bcos.channel.client.Service">
	<property name="orgID" value="WB" />
	<property name="allChannelConnections">
		<map>
		<entry key="WB">
			<bean class="org.bcos.channel.handler.ChannelConnections">
				<property name="connectionsStr">
					<list>
						<value>node1@ip:channelPort</value>
						<value>node2@ip:channelPort</value>
						<value>node3@ip:channelPort</value>
						<value>node4@ip:channelPort</value>
					</list>
				</property>
			</bean>
		</entry>
		</map>
	</property>
</bean>
```

结果如下：

![application.xml.set](https://github.com/FISCO-BCOS/meshchain/raw/master/images/application.xml.set.png)


<span id = "setNameList"></span>
同时修改conf/config.xml里面的setNameList为如下图：

![application.xml.setnamelist](https://github.com/FISCO-BCOS/meshchain/raw/master/images/application.xml.setnamelist.png)


### 然后更改配置文件config.xml
<span id = "config.xml"></span>

```
cd meshchain/dist/conf

#config.xml的内容如下
vim config.xml
```

```
<?xml version="1.0" encoding="UTF-8" ?>
<config>
    <privateKey>bcec428d5205abe0f0cc8a734083908d9eb8563e31f943d760786edf42ad67dd</privateKey> <!--用作发送交易做签名的私钥-->
    <serviceId></serviceId> <!-- HTTP方式可忽略-->
    <scenario></scenario> <!-- Http方式忽略-->
    <routeAddress>${routeAddress}</routeAddress> <!-- 路由合约，这个需要执行下面部署路由合约的时候会返回的地址-->
    <setNameList>set0Chain,set1Chain</setNameList> <!--用户链的列表,名字可以随意，但需要不重复-->
    <hotChainName>hotService</hotChainName> <!--热点链的名字-->
    <routeChainName>routeService</routeChainName> <!--路由链链的名字-->
    <enableTimeTask>1</enableTimeTask> <!--是否开启定时任务 0：不开启 1：开启-->
    <timeTaskIntervalSecond>60</timeTaskIntervalSecond> <!--定时任务间隔，秒为单位-->
</config>

```

需要注意的是routeAddress是需要[部署路由合约](#deploy_route)来得到，请部署后，替换config.xml的${routeAddress}



### 使用工具部署路由合约:

<span id = "deploy_route">执行命令：</span>

```
cd meshchain/dist
java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract deploy conf/route.json
```

<span id="route.json"></span>
route.json 格式:

```
[
    {
        "set_name":"set0Service",
        "set_warn_num":8,
        "set_max_num":10,
        "set_node_list":[]
    },
    {
        "set_name":"set1Service",
        "set_warn_num":8,
        "set_max_num":10,
        "set_node_list":[]
    }
]
```

说明：

1. set_name是指用户链的名字，如果有多个set_name，不能重复，且必须要与config.xml的<setNameList></setNameList>保持一致。请参考[例子](#setNameList)

2. set_warn_num：是指某个set达到set_warn_num则会有相关的日志告警，日志设置请参考conf/log4j2.xml

3. set_max_num：是指某个set最多能容纳set_max_num个用户

4. 路由合约的默认分配规则，一个set的[uid](#uid)个数最大为set_max_num.增加用户则会在set1...诸如类推。

5. 部署期间会有相关的日志'deployContract'输出到终端,来确认是否部署成功。

如果看到'register route contract success.address:'，后面跟着就是路由合约的地址，请填写到conf/config.xml的${routeAddress}


### 使用工具注册热点商户和虚拟商户

```
cd meshchain/dist
java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract registerMerchant merchantId merchantName

```

1. registerMerchant是接口名字
2. 第一个参数是商户id,类型是bytes32，譬如"1"
3. 第二个参数是商户名字,类型是bytes32，譬如“myMerchant”

注册成功后，会有'registerMerchant in ** success'才可以认为成功（** 代表不同的链名字）

### 启动server监听:

http server:

```
cd meshchain/dist
nohup java -cp conf/:apps/*:lib/* -Dserver=http -Dport=8081  org.bcos.proxy.main.Start &
```

监听的是http server,就可以curl发送post http请求，如上述的充值和消费接口

充值协议:

```
curl http://127.0.0.1:8081 -d '{"method":"userDeposit","uid":"1","version":"","contractName":"Meshchain","params":["1000"]}'
```

参数说明：

1. method：Meshchain合约中的某个方法，这里代表用户充值
2. uid：用作路由id，区分哪条用户链的用户 <span id="uid"> </span>
3. version：合约版本
4. contractName：合约名字
5. params:数组，这里第一个参数表示金额，类型uint256

返回结果如下图:

![user_deposit](https://github.com/FISCO-BCOS/meshchain/raw/master/images/user_deposit.png)


消费协议:

```
curl http://ip:port -d '{"method":"consume","uid":"1","version":"","contractName":"Meshchain","params":["1",200]}'
```

参数说明：

1. method：Meshchain合约中的某个方法，这里代表用户uid="1"给商家merchantId="1"消费
2. uid：用作路由id，区分哪条用户链的用户
3. version：合约版本
4. contractName：合约名字
5. params:数组，这里第一个参数表示merchantId，类型bytes32；第二个参数是金额，类型是uint256


返回结果如下图：

![user_consume](https://github.com/FISCO-BCOS/meshchain/raw/master/images/user_consume.png)



response响应:

```
{
	"code":0,
	"data":“”，
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
```

# 验证和关键日志

### 商户的资产查询是否变化
若用户消费了接口，则可以通过命令来查询

```
cd meshchain/dist
java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract queryMerchantAssets  chainName '{"contract":"Meshchain","func":"getMerchantAssets","version":"","params":["merchantId"]}'
```

1. chainName哪一条链,如上述的set0Service，set1Service
2. merchantId商户ID，譬如"1"

### 查询跨链的转账是否成功
1. 首先得确保relay task已经开启
2. 其次子链上面的商户需要有资产，否则会有特别的返回码，返回码请参考Meshchain合约的错误码
3. proxy.log,参考log4j2.xml的配置，查询关键字grep 'RelayTask start',代表子链往热点链开始转账
4. 查询总资产是否平衡，即满足转账前链A的资产,链B的资产...等于转账后的子链A',B'...和热点链H的总和。A + B + ... = A' + B' +... + H (可以通过工具查询queryMerchantAssets)


### 更多工具使用说明

```
cd meshchain/dist
java -cp conf/:apps/*:lib/* org.bcos.proxy.tool.DeployContract
```

注意:由于初始化的原因，需要等待若干秒

然后会有相关的工具使用说明:

```
usage:[deploy nodes.json
      [queryMerchantId $chainName, $requestStr
      [registerMerchant $merchantId, $merchantName
      [queryMerchantAssets $chainName, $requestStr
      [querySetUsers $setIdx(0代表set1, 1代表set2,类推)]
```

1. queryMerchantId 查询所有已存在的商户id，参数chainName为[route.json](#route.json)里面的set_name，requestStr为 '{"contract":"Meshchain","func":"getAllMerchantIds","version":"","params":[]}'
2. queryMerchantAssets 查询商户资产，参数chainName为nodes.json里面的set_name，requestStr为'{"contract":"Meshchain","func":"getMerchantAssets","version":"","params":["$merchantId"]}'
3. deploy 发布路由合约，[route.json](#route.json)见上述
4. querySetUsers 查询某个set的所有用户id，setIdx是一个set数组下标，0代表set0, 1代表set1等等





