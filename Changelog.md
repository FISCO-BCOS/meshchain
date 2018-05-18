### meshchain v1.1.0

(2018-04-27)  
*Update*

我们将当前版本定为meshchain的1.0.0版本，后续我们将在此基础上持续进行版本更新。

1. 新增合约接口: 支持任一账户之间的跨链转账
2. java代码逻辑整理
3. 热点链改为可配置
4. 协议格式不兼容 （用户注册，充值，转账）


### meshchain commit a6eda205c51902edf9b25795638d6b5afde79b2f

(2018-01-31)

*Add*

当前版本特性功能：

1. meshchain是一个proxy代理，负责接受外部的交易请求。
2. meshchain是一个有着路由管理功能，负责把某个id的交易请求路由到某条链上
3. meshchain同时也是一个relayer，负责把某条链上的交易，relay到热点链上