# Phantom-Platform

## 介绍
幻影(Phantom)消息平台，是一款基于Java实现的即时通讯(IM)系统。

提供支持单聊、群聊、SDK等通用的技术通讯组件，开箱即用。

## 软件架构

## 构建

- 项目使用了Lombok, 需要安装IDEA Lombok 插件
- 项目使用IDEA的Protobuf插件，需要执行一下命令


```
cd phantom-common && mvn protobuf:compile
```

配置hostname：

```
localhost mysql-server
localhost zk-0
localhost zk-1
localhost zk-2
localhost redis-0
localhost redis-1
localhost redis-2
localhost redis-3
localhost redis-4
localhost redis-5
localhost kafka-0
localhost kafka-1
localhost kafka-2
# 接入服务所在的公网ip
127.0.0.1 acceptor-server
```

测试方式：

- 启动phantom-business的Application
- 启动phantom-dispatcher的Bootstrap
- 启动phantom-acceptor的Bootstrap
- 启动phantom-client测试包下面的ConsoleClient,启动多个实例
- 试试幻影(Phantom)的威力

## Android SDK

[Phantom-Android](https://github.com/wangjianfengnb/Plantom-Android)


| 功能 | 进度 | 特性 | 
| --- | --- | --- | 
| 认证 | 已完成 | 认证 |
| 单聊消息 | 已完成 |  单聊消息
| timeline模型 | 已完成 | 支持多端同步 |  
| 群聊消息 | 已完成 | 支持群组 |
| 创建群组 | 已完成 |   | 
| IPlist | 已完成 |  支持接入服务、分发服务弹性伸缩 | 
| snowflake算法生成messageId | 已完成 | 分布式唯一ID |
| 单点登录SSO、JWT | 模拟完成| 
| Netty 支持SSL | 已完成 | 
| 消息分库分表 | 未开始 |
| 发红包 | 未开始 |
| 客户端消息状态监听 | 已完成 |
| 重复登录提出kick out | 未开始 | 
  

### 系统整体流程图
![](http://assets.processon.com/chart_image/5ed86ebb637689186215fa97.png)
### 认证流程图

![](http://assets.processon.com/chart_image/5dc53e6ce4b005b5778bd235.png)

### 实现
一条消息在发送的时候，先经过接入系统，然后在接入系统的线程池中运行，将消息转发到分发系统，分发系统内部建立n个Processor线程，消息先根据
接收者ID进行hahs之后路由到一个Processor中执行，接着将消息投递到Kafka中，投递到kafka的时候，根据接受者ID作为key。

这样可以确保在一个链路中，每个用户的消息都是串行化执行的。

### 单聊、群聊

#### 离线消息

特点：离线消息读取频率高

- 写扩散策略：发送消息之后，先把离线消息写入redis中，然后推送消息，推送成功，就删除消息
- 群聊的消息在保存离线消息的时候，为群里每一个人都保一条记录，这样可以保证每个人的消息保存在redis cluster中的不同机器上，根据receiverid哈希，缓解每台机器的压力。

基于Redis的SortedSet存储离线消息


#### 历史消息

历史消息落地数据库，走读扩散原则。就是一条群消息保存到数据库中是一条记录，每个人去拉取群消息的时候，就读到这条消息。

因为历史消息是非常低频的访问行为，所以这个走读扩散原则。

#### 消息分库分表方案

单聊消息，根据senderid做分区key。同步AB的消息，先找出A发送给B的消息，然后找出B发送给A的消息。都会路由到同一个数据库的同一个表

群聊消息：根据grouid做分区key，同一个群聊的消息都路由到同一个数据库的同一个表中。

### messageId唯一ID生成

采用Snowflake算法生成唯一ID

### 消息timeline模型

![](http://assets.processon.com/chart_image/5dc905e1e4b0ffd214440983.png)

Timeline模型类似每个人都有一个离线消息队列，对于多端来说，为每一个端维护一个同步的offset，每次抓取数据的时候从offset开始抓。

从而实现多端离线消息同步的功能。

目前的实现方式是在服务端为每个用户生成一个严格的消息sequence,然后在客户端发送消息的时候，

分发系统的processor、kafka的partition都会根据接收者Id进行hash路由，确保消息发送的顺序

### zookeeper工作流程

### 客户端获取分发系统ip地址场景

接入系统启动的时候往zookeeper中注册，客户端启动的时候根据接口获取其中一个"最适合的"接入系统的ip地址。

如果客户端和接入系统断开连接，则继续选择一个接入服务重新连接


#### 接入系统获取分发系统地址列表场景

分发系统启动的时候往zookeeper中注册，接入系统会从zookeeper中获取到一个分发系统的ip地址。

断开连接会无限重连。


### 单点登录方案

基于SSO单点登录系统实现用户认证，生成JWT串，在分发系统可以直接基于secret解密JWT从而实现认证功能。

### 容错性分析

#### 客户端网络环境不好，挂了怎么处理
 - 客户端挂了，接入系统会移除session、包括redis中的session。等待客户端重新连接。
 - 在客户端挂了这段时间，分发系统发送消息的时候会发现推送不成功，此时要放入离线消息。


#### 接入系统挂了怎么处理
 - 对于分发系统来说，通过zookeeper感知到，需要移除分发系统实例。但是此时会发现有些消息推送对应的接入系统找不到了，
此时消息进入离线消息。等待客户端重新连接的时候，发起认证，更新了接入系统的地址，再将消息推送过去
 - 对于客户端来说，分发系统挂了，则继续从zookeeper中查找下一个可用的接入系统地址，发起连接，重新认证。
- 假设分发系统收到了消息分发任务，但是此时对应的接入系统宕机了。此时消息会被阻塞一段时间(无限循环,直到等待到对应的接入系统和分发系统建立连接)(后续待优化)

#### 分发系统挂了怎么处理

- 对于接入系统来说，通过zookeeper感知到，需要移除分发系统的实例，如果分发系统上线了，则需要建立连接。
- 初版本设计的时候，每个用户的session中保存了接入系统和分发系统的channelId，在推送消息的时候，根据Session获取到channelId推送到对应的接入服务。

考虑这样一种场景：

>> 假设用户A连接到接入服务器acceptor1,然后接入服务器acceptor1和分发服务器dispatcher1建立连接，
那么用户A的Session中保存了接入服务和分发服务的channelId1，假设dispatcher1服务器挂了，重启了，他们的连接ID变为了
channelId2，但是此时用户A的Session中的还是channelId1,会导致分发服务器在转发用户A的时候找不到接入服务器。

解决办法：
  
>> 对于接入系统，系统启动之后，会产生一个接入系统的唯一标识acceptorInstanceId，然后接入系统维持连接的每个用户的session中都会保存这个
acceptorInstanceId，然后分发系统内部会维持一个acceptorInstanceId -> channel的映射关系，假设分发系统当机了，接入系统重新连接的时候，
将自己的acceptorInstanceId推送给服务端。


如果分发服务器宕机了，那么对于每个用户而言，他们的路由的分发服务器改变了，此时可能会导致某些用户的消息任务分别在两台服务器的队列中

- 举个例子，有3台分发服务器a，b, c，用户A的消息处理经过hash分发之后，落在了a机器，此时c机器宕机了，用户A的消息hash后落在b机器，
此时会导致，ab机器都在处理用户A的消息，可能导致消息乱序
- 另外如果在负载高的时候，内存队列里面还有任务没有发送，可能此时会导致消息丢失，如果是发送消息的过程中宕机了，此时客户端超时无法收到响应，消息发送失败。
  - 如果在发送响应消息的过程中，在内存队列里面丢失了，那就可能导致这条消息在发送方看来是没有发送成功的。
  - 如果在推送消息的时候，刚消费消息，放入异步队列，然后宕机了，这个时候，会导致消息推送失败，并且没有进入离线消息中。
  
上述问题可以考虑采用异转同步的方式，等消息处理完毕之后，在提交kakfa的offset，这样如果宕机后，重启可以重新消费这条消息。
但是这样一来会导致消费kafka这个过程变慢。 (需要权衡)
  

### SSL加密通讯
```

# 创建服务端秘钥
keytool -genkey -alias phantom-server -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass phantom-server -storepass phantom-server -keystore phantom-server.jks

# 导出服务端秘钥
keytool -export -alias phantom-server -keystore phantom-server.jks -storepass phantom-server -file phantom-server.cer
		
# 创建客户端秘钥
keytool -genkey -alias phantom-client -keysize 2048 -validity 365  -keyalg RSA -dname "CN=localhost" -keypass phantom-client  -storepass phantom-client -keystore phantom-client.jks

# 创建导出客户端秘钥
keytool -export -alias phantom-client -keystore phantom-client.jks -storepass phantom-client -file phantom-client.cer

# 将服务端的证书导入到客户端的信任证书仓库中
keytool -import -trustcacerts -alias phantom-server -file phantom-server.cer -storepass phantom-client -keystore phantom-client.jks

# 将客户端的证书导入到服务端的信任证书仓库中
keytool -import -trustcacerts -alias phantom-client -file phantom-client.cer -storepass phantom-server -keystore phantom-server.jks
```

#### 参与贡献
...

