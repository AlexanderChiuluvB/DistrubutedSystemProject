# DistrubutedSystemProject
2019秋分布式系统
### 秒杀系统架构设计

#### 业务特点

- 瞬时并发量大，秒杀时候会有大量用户在同一时间进行抢购,并发访问量会突然暴增
- 库存量少，一般用户数目远远大于库存数量，需要防止超卖现象出现

- 业务流程简单: 下订单->数据库的库存减少->支付订单
- 网络流量暴增，对应网络带宽的突然增加
- 读多写少:一趟火车其实只有2000张票，200w个人来买，最多2000个人下单成功，其他人都是查询库存，写比例只有0.1%，读比例占99.9%

#### 架构设计思想

- 限流

  限制大部分用户流量，只允许少量用户进入后端服务器

- 削峰

　　通过使用缓存和消息队列中间件把瞬间的流量高峰变得平缓

- 异步

  异步其实也是削峰的一种实现方式，可以提高系统并发量

- 内存缓存

  数据库读写为磁盘IO,因此可以把部分数据或者业务逻辑转移到内存缓存(Redis)

核心思想:

- 尽量把请求**拦截在上流**，层层过滤，通过充分利用**缓存与消息队列**，提高请求处理速度以及削峰，根本核心是最终减轻对数据库的压力.如果不在上流拦截，会导致数据库读写锁冲突变得严重，并且导致死锁，最终请求超时．

- 优化方案主要放在**服务端优化**与**数据库优化**

  ```
  服务端优化：并发处理，队列处理
  数据库优化：数据库缓存，分库分表，分区操作，读写分离，负载均衡
  ```

  

#### 业务流程

- 架构图

完全版

![整体流程](https://raw.githubusercontent.com/qiurunze123/imageall/master/miaosha.png)



简要版

![img](http://spider.ws.126.net/861928df66efcc4335155f64c0c31905.jpeg)

Nginx:反向代理＆负载均衡，把用户的请求分发到不同的机器上

Redis: 内存缓存:实现分布式锁，提高读写速度，分担数据库压力

Kafka: 消息队列,削峰,处理流量猛增的情况，拦截大量并发请求，同时实现了消息异步处理

MYSQL: 持久化存储商品信息，实现数据的强一致性检验，同时大规模高并发下要实现分库分表，读写分离[mysql在高并发场景中使用](https://blog.csdn.net/qq_36236890/article/details/82390412)

大概流程如下，rabbitMQ也是一种消息队列

![img](https://img-blog.csdnimg.cn/20181210152632678.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3poYW5nbGlfd2VpMDQwMw==,size_16,color_FFFFFF,t_70)

#### 具体设计流程

我们暂时忽略前端层只关注后端

#### 实现细节(未完成)

- 可以把数据库中的库存数据转移到Redis缓存中，所有减库存操作都在Redis中进行，然后再通过后台进程把redis中的用户秒杀请求同步(RPC调用)到数据库中

  > 采用Redis的list数据结构，把每个商品作为key，把用户id作为value，队列的长度就是库存数量。对于每个用户的秒杀，使用 RPUSH key value插入秒杀请求， 当插入的秒杀请求数达到上限时，停止所有后续插入。然后根据先进先出，使用 LPOP key zhuge读取秒杀成功者的用户id，再操作数据库做最终的下订单减库存操作。

- 异步:使用消息队列：把请求写到消息队列中，数据库层订阅消息减库存，减库存成功的请求返回秒杀成功，失败的返回秒杀结束。
- 使用集群：在网站高并发访问的情况下，使用负载均衡技术为一个应用构建一个由多台服务器组成的集群，将并发访问请求分发到多台服务器上处理。 
- MYSQL批量入库，提高insert效率

具体实现:代码实例请见[架构实战](https://www.cnblogs.com/andy-zhou/p/5364136.html#_caption_2)

> 1. 用户请求分发模块：使用Nginx或Apache将用户的请求分发到不同的机器上。
> 2. 用户请求预处理模块：判断商品是不是还有剩余来决定是不是要处理该请求。
> 3. 用户请求处理模块：把通过预处理的请求封装成事务提交给数据库，并返回是否成功。
> 4. 数据库接口模块：该模块是数据库的唯一接口，负责与数据库交互，提供RPC接口供查询是否秒杀结束、剩余数量等信息。


##### Kafka Asynchronous

Kafka 异步削峰 与 Redis耦合的示意图

![img](https://raw.githubusercontent.com/gongfukangEE/gongfukangEE.github.io/master/_pic/%E5%88%86%E5%B8%83%E5%BC%8F/%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97%E7%BC%93%E5%86%B2.png)



#### 解决数据安全问题

防止超卖，即售出数量>库存数量

1.Redis自带的乐观锁机制，用版本号更新

> 实现的流程为：这个数据所有请求都有资格去修改，但会获得一个该数据的版本号，只有版本号符合的才能更新成功，其他的返回抢购失败。 

2.从SQL入手

```sql
UPDATE table_name SET n=n-1 WHERE n>1; 
```



#### 技术栈(未完成)

- 主要开发语言: JAVA

  建议使用　IntelliJ Idea 2019.3 企业版

- 项目框架: Spring Boot

- RPC通信框架　Apache dubbo

- 消息队列 Kafka

- 内存数据库 Redis

- 数据库 MYSQL

- 反向传播与负载均衡 Nginx

- 容器化部署 Docker

- 压力测试　Jmeter

  

#### ref

[淘宝双十一秒杀架构方案](https://blog.csdn.net/github_37048196/article/details/83573935)

[秒杀架构实践](http://dy.163.com/v2/article/detail/E1JRU1AP0511Q1AF.html)

[秒杀架构](https://www.cnblogs.com/andy-zhou/p/5364136.html)




## 资料汇总

请先参考这个,以更好完成git 团队协作开发
[git 团队协作](https://www.cnblogs.com/nongzihong/p/10448516.html). 

[JAVA分布式架构](https://www.cnblogs.com/nongzihong/p/10448516.html)

### RPC架构示例

https://github.com/SwordfallYeung/CustomRpcFramework
