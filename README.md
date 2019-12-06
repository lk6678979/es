# Sprinboot2.0.3整合Elasticsearch(基于TCP的TransportClient方式实现，ES7后不支持）

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
## 3 索引操作
### 3.1 创建索引
* 注意：只会创建空索引，不会设置mapping，需要单调调用putMapping
```java
//根据配置好映射关系的类对象，创建索引（反射获取到注解中的参数去创建索引）
<T> boolean createIndex(Class<T> clazz);

//指定索引名创建索引，创建出的所有没有属性
boolean createIndex(String indexName);

//指定索引名和配置创建索引
//setting支持3种类型数据：string，map，XContentBuilder，一般使用map的kv格式设置配置
boolean createIndex(String indexName, Object settings);

//根据配置好映射关系的类对象，并制定相关配置，创建索引（反射获取到注解中的参数去创建索引）
//setting支持3种类型数据：string，map，XContentBuilder，一般使用map的kv格式设置配置
<T> boolean createIndex(Class<T> clazz, Object settings);
```

### 3.2 映射字段
```java

//根据配置好映射关系的类对象，设置字段映射
<T> boolean putMapping(Class<T> clazz);

//根据索引名称，文档类型和mapping对象，设置字段映射
boolean putMapping(String indexName, String type, Object mapping);

//根据包含索引映射的class和mapping对象，设置字段映射
<T> boolean putMapping(Class<T> clazz, Object mapping);

```

### 3.3 删除索引
```java

//根据配置好映射关系的类对象，删除索引
<T> boolean deleteIndex(Class<T> clazz);

//根据索引名称删除索引
boolean deleteIndex(String indexName);

```

## 4. 新增文档数据（继承ElasticsearchRepository）
### 4.1 Repository接口
Spring Data 的强大之处，就在于你不用写任何DAO处理，自动根据方法名或类的信息进行CRUD操作。只要你定义一个接口，然后继承Repository提供的一些子接口，就能具备各种基本的CRUD功能。
来看下ElasticsearchCrudRepository接口：
```java
package org.springframework.data.elasticsearch.repository;

import java.io.Serializable;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @param <T>
 * @param <ID>
 * @author Rizwan Idrees
 * @author Mohsin Husen
 */
@NoRepositoryBean
public interface ElasticsearchRepository<T, ID extends Serializable> extends ElasticsearchCrudRepository<T, ID> {

	<S extends T> S index(S entity);

	Iterable<T> search(QueryBuilder query);

	Page<T> search(QueryBuilder query, Pageable pageable);

	Page<T> search(SearchQuery searchQuery);

	Page<T> searchSimilar(T entity, String[] fields, Pageable pageable);

	void refresh();

	Class<T> getEntityClass();
}
```
所以，我们只需要定义接口，然后继承它就OK了。
```java
/**
      * @Description:定义ItemRepository 接口
      * @Param:
      * 	Item:为实体类
      * 	Long:为Item实体类中主键的数据类型
      * @Author: https://blog.csdn.net/chen_2890
      * @Date: 2018/9/29 0:50
       */	 
public interface ItemRepository extends ElasticsearchRepository<Item,Long> {

}
```
接下来，我们测试新增数据：

### 4.2 新增一个对象
```java
@Autowired
private ItemRepository itemRepository;

 @Test
    public void insert() {
        Item item = new Item();
        item.setId(4L);
        item.setPrice(55.09D);
        item.setCategory("组织组织sfaassd");
        item.setBrand("g3123第三方dgh1212");
        item.setTitle("地方个花哥哈哈");
        item.setImages("65阿萨德的");
        itemRepository.save(item);
    }
```
### 4.3 批量增加多个
```java
@Autowired
private ItemRepository itemRepository;

 @Test
    public void insertList() {
        Item item = new Item();
        item.setId(11L);
        item.setPrice(5995.09D);
        item.setCategory("");
        item.setBrand("g3123第三方dgh1212");
        item.setTitle("地方个花哥哈哈");
        item.setImages("65阿萨德的");
        Item item2 = new Item();
        item2.setId(12L);
        item2.setPrice(54345.09D);
        item2.setCategory("123faassd");
        item2.setBrand("zzzz方dgh1212");
        item2.setTitle("地方个花12哈哈");
        item2.setImages("65阿萨德的");
        Item item3 = new Item();
        item3.setId(13L);
        item3.setPrice(5645.09D);
        item3.setCategory("124织124sfaassd");
        item3.setBrand("xcw213第三方dgh1212");
        item3.setTitle("768哥哈哈");
        item3.setImages("xxx的");
        List<Item> list = new ArrayList<>();
        list.add(item);
        list.add(item2);
        list.add(item3);
        itemRepository.saveAll(list);
    }
```
### 4.4 修改
elasticsearch中本没有修改，它的修改原理是该是先删除在新增

修改和新增是同一个接口save，区分的依据就是id。save根据id会去覆盖同id的数据
## 5. 查询
### 5.1 基本查询
ElasticsearchRepository提供了一些基本的查询方法：
![](https://github.com/lk6678979/image/blob/master/es-1.jpg)
