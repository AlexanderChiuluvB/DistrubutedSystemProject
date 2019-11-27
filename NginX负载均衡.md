## NginX负载均衡

### 算法

RR(Round-Robin):对向服务器发出的请求用RR算法

least-connected:把请求发送给有最少连接的服务器

ip-hash:用基于ip地址的哈希函数决定哪一个服务器会被选给下一个请求

### 使用方法

可以指定application的weight来过滤请求,问题是如何做到按时间来过滤请求,不能平均过滤.

默认使用RR算法,如果要用least-connected算法,需要加指令least_conn在upstream模块中.

#### 配置configure文件

结构:

http

​	upstream

​		server 

​		server

​	server

​		location

​			root URI会被加到root后面指定的路径,以寻找请求的资源,如果有多个			location块,nginx会选择最长的那个.



#### 四个负载均衡工具

least_conn

ip_hash

weight

health 

#### 代理



#### upstream方法

#### stream模块代替http模块





###结构描述

### example code

```
http {
	upstream myapp1 {
		least_conn;//elective
		ip_hash;
		server srv1.example.com;
		server srv2.example.com;
		server srv3.example.com;
	}
	
	server {
		listen 80;
		
		location / {
			proxy_pass http://myapp1;
			
		}
	}
}
```

upstream下的server是同一个application的两个实例.分别运行在srv1和srv2上.

(什么application?application如何指定??)

所有的请求都会被代理到服务器群myapp1上

### 其他

分发的是请求,不是客户,所以不是一个客户一直会被指定到同一个服务器上.如果有需要以客户为单位分配服务器,那么就用ip-hash.

