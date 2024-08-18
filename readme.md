# MikuMQ - 基于AIO的新一代消息队列

MikuMQ基于高性能的AIO网络框架——Smart-Socket编写，内存占用更低，且支持复杂的消息投递规则，可以对消息的投递进行更好的管控。

MikuMQ初步实现了常见MQ的大部分功能，但仍需打磨。

[使用文档](https://github.com/AGoodYear/MikuMQ/tree/main/docs)

# 快速开始

[MikuMQ/docs/1.快速开始.md at main · AGoodYear/MikuMQ (github.com)](https://github.com/AGoodYear/MikuMQ/blob/main/docs/1.快速开始.md)

# 项目结构

```
-broker  （Broker服务器）
│  ├─src
│  │  ├─main
│  │  │  ├─java
│  │  │  │  └─com
│  │  │  │      └─ivmiku
│  │  │  │          └─mikumq
│  │  │  │              ├─core  （服务器核心）
│  │  │  │              │  ├─manager
│  │  │  │              │  └─server
│  │  │  │              ├─dao   （封装数据库操作）
│  │  │  │              ├─tracing  （预留的状态追踪接口）
│  │  │  │              │  └─entity
│  │  │  │              └─utils  （工具）
-client  （客户端）
│  ├─src
│  │  ├─main
│  │  │  ├─java
│  │  │  │  └─com
│  │  │  │      └─ivmiku
│  │  │  │          └─mikumq
│  │  │  │              ├─connection  （创建连接）
│  │  │  │              ├─consumer   （消费者客户端）
│  │  │  │              ├─exception  （异常）
│  │  │  │              └─producer   （生产者客户端）
-commons  （公共模块）
│  ├─src
│  │  ├─main
│  │  │  ├─java
│  │  │  │  └─com
│  │  │  │      └─ivmiku
│  │  │  │          └─mikumq
│  │  │  │              ├─entity
│  │  │  │              ├─request
│  │  │  │              └─response
-springboot-starter 
│  ├─src
│  │  ├─main
│  │  │  ├─java
│  │  │  │  └─com
│  │  │  │      └─ivmiku
│  │  │  │          └─mikumq
│  │  │  │              ├─annotations
│  │  │  │              ├─aspect
│  │  │  │              ├─config
│  │  │  │              └─core
```

