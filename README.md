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
     * @Description: @Id注解必须是springframework包下的
     * org.springframework.data.annotation.Id
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
### 2.3 ElasticsearchTemplate中提供了操作ES的完整API
```
   @Autowired
   private ElasticsearchTemplate elasticsearchTemplate;
```
## 3 API介绍
### 3.1 创建索引
```java
//根据配置好映射关系的类对象，创建索引（反射获取到注解中的参数去创建索引）
<T> boolean createIndex(Class<T> clazz);

//指定索引名创建索引
boolean createIndex(String indexName);

//指定索引名和配置创建索引
boolean createIndex(String indexName, Object settings);

//根据配置好映射关系的类对象，并制定相关配置，创建索引（反射获取到注解中的参数去创建索引）
//setting支持3种类型数据：string，map，XContentBuilder
<T> boolean createIndex(Class<T> clazz, Object settings);
```

