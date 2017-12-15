# 快捷部署多链
步骤一：

```
1. git clone https://github.com/FISCO-BCOS/FISCO-BCOS.git
2. sh build.sh
3. cd FISCO-BCOS/sample
4. sh init_meshchain.sh <链的数目> <每条链的节点数目> <ip0> <ip1>...
```

其中，链数目>=4 节点数目>=1 后面的ip是表示，ip0部署链0， ip1部署链1等等。默认情况下，链0部署路由链，链1部署热点链。其他则部署用户链，也称为set链

步骤二：生成每个ip的tar.gz压缩包，需要在对应的机器ip上面部署，部署的目录可以任意。如xxx.xxx.xxx.xxx.tar.gz和proxyConfig.tar.gz

步骤三：每个xxx.xxx.xxx.xxx.tar.gz，通过scp或者其他方式传输eth包到对应机器任一目录，执行：

```
1. tar -zxvf xxx.xxx.xxx.xxx.tar.gz
2. cd xxx.xxx.xxx.xxx
3. sh start_meshchain.sh
4. 最后输出deploy Meshchain.sol success...代表成功
```

步骤四：proxyConfig.tar.gz中的文件需要拷贝到[relay中继](https://github.com/FISCO-BCOS/meshchain.git) conf/下面的对应文件，执行

```
1. git clone https://github.com/FISCO-BCOS/meshchain.git
2. cd meshchain
3. gradle build
4. cd dist
5. 拷贝步骤二生成的proxyConfig.tar.gz到当前目录
6. tar -zxvf proxyConfig.tar.gz
7. cp proxyConfig/* conf/
8. 按需修改conf/log4j2.xml的日志配置
9. sh start.sh 默认监听8081端口。由于初始化需要一段时间，请耐心等待若干秒，然后用命令netstat -npl | grep 8081 来查看进程是否已经监听
10. sh demo.sh 功能包括了对热点账户的注册，用户的充值和消费接口，期间需要输入要注册的热点账户Id,请参考提示操作
11. sh queryAssets.sh 表示每隔一秒去查询热点账户在多条链之间的资产变化，期间需要输入要查询的热点账户Id
```
