# Elasticsearch Java API实现方式 
## 1. 传输客户端（Transport client）
  传输客户端作为一种轻量级客户端，本身不加入集群，只是简单的发送请求到远端集群中的节点,作为一个集群和应用程序之间的通信层。它知道 API 并能自动帮你在节点之间轮询，帮你嗅探集群等等。但它是集群 外部的 ，和 REST 客户端类似。我们访问的节点负责收集各节点返回的数据（query fetch merge），最后一起返回给客户端。构造transportClient时候，需要指定sniff是否为true。Transport是连接到Elasticsearch的本地方法之一。它是官方Elasticsearch分发的一部分，因此需要客户端用Java编写（或至少在JVM上运行）。 它非常快，在JVM上本机运行。序列化是有效的，发送到/从Elasticsearch实例的消息和操作中几乎没有开销。它需要保持Elasticsearch服务器和客户端版本有些同步。在Elasticsearch 1.0之前，将需要完全相同的版本，但较新的版本（1.0和更高版本）支持版本之间的交互。由于异常序列化和更新之间的其他潜在细微差异，在客户端和服务器上运行相同的JVM更新版本也是有益的。 目前不支持加密或身份验证，但是宣布不久会满足这些需求。要在Found.no托管集群上使用传输客户端，可以使用elasticsearch自定义传输模块，该模块负责加密，身份验证和保持活动。
```xml
<dependency>
      <groupId>org.elasticsearch.client</groupId>
      <artifactId>transport</artifactId>
      <version>5.0.2</version>
    </dependency>
```
* 7.X之后不再支持
* springboot-data已集成
* 参考项目 https://github.com/lk6678979/es/tree/master/es-spring-data

## 2. TTP客户端（Rest Client）
  HTTP在大多数编程语言中得到很好的支持，这是连接到Elasticsearch的最常见的方法。如果要使用HTTP，还有一个重要的选择：使用一个现有的Elasticsearch基于HTTP的库，或者只是创建一个小的包装器，需要使用HTTP客户端的操作。 由于HTTP是一个通用协议，并支持各种各样的用例，一些重要的事情需要由客户端实现：连接池和保持活动。需要连接池以避免必须支付每个请求的TCP连接建立成本。更重要的，如果它使用HTTPS，这带来额外的加密握手成本。连接池经常需要保持活动支持，因为我们希望避免连接由于空闲而中断。 虽然最初显而易见的是，连接建立实际上是重要的，但是考虑建立TCP连接需要三次握手。简单地说，使用50毫秒的ping时间，除了获取和释放本地资源（处理客户端端口，连接管理等）所花费的时间之外，建立连接需要大约75毫秒 - 这个没有考虑在两端处理请求/响应（例如，串行化）所花费的时间。没有连接池，这个时间被添加在每个请求的顶部。对于我们建议用于安全和隐私的HTTPS，连接建立开销有时可以以秒为单位测量，这甚至更显着。考虑到最终用户的响应时间必须在100毫秒以下才能被观察为“即时”的基本建议，即使非加密的开销也使得这种限制几乎不可能保持在内。 由Elasticsearch编写和支持的官方（非Java）客户端都使用HTTP底层与Elasticsearch进行通信。一般建议是使用封装HTTP API的正式客户端，因为他们负责处理所有这些细节。 HTTP客户端实现可能相当快，其中一些甚至与本机协议的速度竞争。 Elasticsearch的HTTP API被广泛使用，并且具有相当多的社区支持。然而，性能取决于客户端库，并且通常需要进行配置或调整才能最大化。
```xml
 <dependency>
      <groupId>org.elasticsearch.client</groupId>
      <artifactId>rest</artifactId>
      <version>5.0.2</version>
    </dependency>
```
## 3. Jest
Jest是ElasticSearch的Java HTTP Rest客户端。 Jest填补了 ElasticSearch 缺少 Http Rest接口 客户端的空白。 
通过Maven编译实现，需要设定好不同的版本。现在pom.xml最新的Elasticsearch版本为5.3.3（2017年5月27日），5.3.3的API向ES2.3.4插入索引数据，不能保障数据正常插入，且没有报错。
### 1.3.1 Jest介绍
Jest是Elasticsearch 的Java Http Rest 客户端。
ElasticSearch已经具备应用于Elasticsearch内部的Java API，但是Jest弥补了ES自有API缺少Elasticsearch Http Rest接口客户端的不足。
### 1.3.2 Jest优势概括如下：
* 提供Restful API， 原生ES API不具备；
* 若ES集群使用不同的ES版本，使用原生ES API会有问题，而Jest不会；
* 更安全（可以在Http层添加安全处理）。
### 1.3.3 pom
概括下，Jest使用maven的方式管理项目。支持最新的Elasticsearch。 
pom.xml有相关配置信息。（默认是最新ES版本相关的pom.xml,截止：2017-08-13 16:38, 其默认支持版本5.3.2）。
```xml
<dependency>
  <groupId>io.searchbox</groupId>
  <artifactId>jest</artifactId>
  <version>2.0.0</version>
</dependency>
```
高版本ES也是可以使用的
### 1.3.4 使用
![](https://github.com/lk6678979/image/blob/master/es-4.jpg)

