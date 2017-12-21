# 快捷部署多链

## 部署单机N链
步骤一：

```
git clone https://github.com/FISCO-BCOS/FISCO-BCOS.git
sh build.sh
cd FISCO-BCOS/sample
sh init_meshchain.sh 4 4 127.0.0.1 127.0.0.1 127.0.0.1 127.0.0.1
```

步骤二：

```
#启动路由链，执行过程需要较长时间，请耐心等待
tar -zxvf 127.0.0.1_route.tar.gz && rm 127.0.0.1_route.tar.gz
cd 127.0.0.1_route/
sh start_meshchain.sh


#启动热点链
tar -zxvf 127.0.0.1_hot.tar.gz && rm 127.0.0.1_hot.tar.gz
cd 127.0.0.1_hot/
sh start_meshchain.sh

#用户链0
tar -zxvf 127.0.0.1_set0.tar.gz && rm 127.0.0.1_set0.tar.gz
cd 127.0.0.1_set0/
sh start_meshchain.sh

#用户链1
tar -zxvf 127.0.0.1_set1.tar.gz && rm 127.0.0.1_set1.tar.gz
cd 127.0.0.1_set1/
sh start_meshchain.sh


#切换回到sample目录
cd ..

```


步骤三：

```
git clone https://github.com/FISCO-BCOS/meshchain.git
cd meshchain

#gradle安装说明，请参照https://gradle.org/install/
#java安装说明，请参照http://www.oracle.com/technetwork/java/javase/downloads/index.html
#确保PATH里面已经追加gradle，java，譬如PATH=$JAVA_HOME/bin:$GRADLE_HOME/bin:$PATH

gradle build
cd dist

#拷贝步骤一生成的proxyConfig.tar.gz到当前目录
mv ../../proxyConfig.tar.gz ./ && tar -zxvf proxyConfig.tar.gz && cp proxyConfig/* conf/ && rm -rf proxyConfig

#按需修改conf/log4j2.xml的日志路径，默认/tmp/proxy.log

#启动http server 默认监听8081端口
sh start.sh

#由于初始化需要一段时间，请耐心等待若干秒，然后用命令netstat -npl | grep 8081 来查看进程是否已经监听

netstat -npl | grep 8081

#demo相关的跨链交易操作 
sh demo.sh
```

##多机部署多链

步骤一：

```
git clone https://github.com/FISCO-BCOS/FISCO-BCOS.git
sh build.sh
cd FISCO-BCOS/sample

#其中，链数目>=4 节点数目>=1 后面的ip是表示，ip0部署链0， ip1部署链1等等。默认情况下，链0部署路由链，链1部署热点链。其他则部署用户链，也称为set链
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

步骤一生成的proxyConfig.tar.gz中的文件需要拷贝到部署[relay中继](https://github.com/FISCO-BCOS/meshchain.git) conf/下面的对应文件，执行

```
git clone https://github.com/FISCO-BCOS/meshchain.git
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

#demo相关的跨链交易操作 
sh demo.sh
```
