# Sprinboot2.0.3整合Elasticsearch

## 1. 搭建基础项目

### 1.1 创建一个普通的maven项目
也可直接去``https://start.spring.io/``下载包含依赖的项目

### 1.2 创建完的工程pom.xml文件中的依赖如下：

```xml
 <!-- 继承springboot项目-->
   <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.3.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <java.version>1.8</java.version>
        <spring_boot.version>2.0.3.RELEASE</spring_boot.version>
    </properties>

   <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <!-- 是否打包为可执行jar包-->
                    <executable>true</executable>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

## 2.代码编写
### 2.1 添加ES所需配置
```yml
spring:
  data:
    elasticsearch:
      #可以通过es的9200端口查询，例如http://elk01:9200
      cluster-name: my_cluster
      #es节点，都好分割多个节点，端口号9300
      cluster-nodes: elk01:9300,elk02:9300,elk03:9300
```
### 2.2 编写实体映射类
```java
package com.lk.es.entity.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 用户信息
 * ------------------------------------------------------
 *
 * @Document 作用在类，标记实体类为文档对象，一般有两个属性
 * · indexName：对应索引库名称
 * · type：对应在索引库中的类型
 * · shards：分片数量，默认5
 * · replicas：副本数量，默认1
 * ------------------------------------------------------
 * @Id 作用在成员变量，标记一个字段作为id主键
 * ------------------------------------------------------
 * @Field 作用在成员变量，标记为文档的字段，并指定字段映射属性：
 * ·type：字段类型，是枚举：FieldType，可以是text、long、short、date、integer、object等
 * ··text：存储数据时候，会自动分词，并生成索引
 * ··keyword：存储数据时候，不会分词建立索引
 * ··Numerical：数值类型，分两类
 * ···基本数据类型：long、interger、short、byte、double、float、half_float
 * ···浮点数的高精度类型：scaled_float,需要指定一个精度因子，比如10或100。elasticsearch会把真实值乘以这个因子后存储，取出时再还原
 * ··Date：日期类型,elasticsearch可以对日期格式化为字符串存储，但是建议我们存储为毫秒值，存储为long，节省空间
 * ·index：是否索引，布尔类型，默认是true
 * ·store：是否存储，布尔类型，默认是false
 * ·analyzer：分词器名称，这里的ik_max_word即使用ik分词器
 */
@Data
@Document(indexName = "item", type = "docs", shards = 3, replicas = 2)
public class Item {
    /**
     * 主键ID
     */
    @Id
    private Long id;

    /**
     * 标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    /**
     * 分类
     */
    @Field(type = FieldType.Keyword)
    private String category;
    /**
     * 品牌
     */
    @Field(type = FieldType.Keyword)
    private String brand;
    /**
     * 价格
     */
    @Field(type = FieldType.Double)
    private Double price;
    /**
     * 图片地址
     */
    @Field(index = false, type = FieldType.Keyword)
    private String images;
}
```
### 2.2 创建3个配置文件，applycation-one.yml,applycation-two.yml,applycation-three.yml(用于注册中心高可用集群)，其中一个配置文件如下，3个配置文件指定不同的server.port，并在defaultZone中配置其他2个yml启动的服务器的ip端口
```yml
server:
  #服务启动端口号
  port: 8806
spring:
  #服务名称
  application:
    name: eureka-server
#eureka服务注册发现中心配置
eureka:
  #服务配置
  server:
    #是否启用注册中心的保护机制，Eureka 会统计15分钟之内心跳失败的比例低于85%将会触发保护机制，不剔除服务提供者，如果关闭服务注册中心将不可用的实例正确剔除
    enable-self-preservation: false
  instance:
    #是否使用ip注册（默认使用域名注册）
    preferIpAddress: true
    #健康检查页面的URL，1.X版本默认/health，2.X版本默认/actuator/health，一般不需要更改
    health-check-url-path: /actuator/health
  client:
    #是否将注册中心本身也注册到注册中心中
    registerWithEureka: true
    #此客户端是否获取eureka服务器注册表上的注册信息（注册中心不会去调用其他服务，所以不需要获取注册信息）
    fetchRegistry: false
    serviceUrl:
      #注册中心URL，配置需要注入的配置中心，如果有2个注册中心则需要配置2个，如果有3个，则只需要注入除自己的另外2个
      defaultZone: http://127.0.0.1:8807/eureka/,http://127.0.0.1:8807/eureka/
management:
  endpoints:
    web:
      exposure:
        #开放所有页面节点  默认只开启了health、info两个节点
        include: "*"
  endpoint:
    health:
      #显示健康具体信息  默认不会显示详细信息
      show-details: ALWAYS
```
### 2.3 启动
#### 2.3.1 使用maven打包项目
#### 2.3.2 启动jar
依次执行下面指令启动3个集群的注册中心：  
	java -jar eureka-server-1.0.0.jar --spring.profiles.active=one  
	java -jar eureka-server-1.0.0.jar --spring.profiles.active=two  
	java -jar eureka-server-1.0.0.jar --spring.profiles.active=three  
## 2.4可视化界面
在浏览器依次打开:  
http://127.0.0.1:8806/  
http://127.0.0.1:8807/  
http://127.0.0.1:8808/  
界面效果如下：
![](https://github.com/lk6678979/image/blob/master/spring-cloud/eureka-ui.png)  
	
