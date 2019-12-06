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
* 我们来试试查询所有：
```
    // 查找所有
    //Iterable<Item> list = this.itemRepository.findAll();
    // 对某字段排序查找所有 Sort.by("price").descending() 降序
    // Sort.by("price").ascending():升序
    @Test
    public void query() {
        Iterable<Item> list = this.itemRepository.findAll(Sort.by("price").ascending());

        for (Item item : list) {
            System.out.println(item);
        }
    }
```
### 5.2 自定义方法
Spring Data 的另一个强大功能，是根据方法名称自动实现功能。  
比如：你的方法名叫做：findByTitle，那么它就知道你是根据title查询，然后自动帮你完成，无需写实现类。  
当然，方法名称要符合一定的约定：  
![](https://github.com/lk6678979/image/blob/master/es-2.jpg)
例如，我们来按照价格区间查询，定义这样的一个方法：
```java
ublic interface ItemRepository extends ElasticsearchRepository<Item,Long> {

    /**
     * @Description:根据价格区间查询
     * @Param price1
     * @Param price2
     */
    List<Item> findByPriceBetween(double price1, double price2);
}
```
不需要写实现类，然后我们直接去运行：
```java
    /**
     * @Description:按照价格区间查询
     */
    @Test
    public void queryByPriceBetween(){
        List<Item> list = this.itemRepository.findByPriceBetween(2000.00, 6000.00);
        for (Item item : list) {
            System.out.println("item = " + item);
        }
    }
```
### 5.3 自定义查询
```java
 /**
     * @Description:matchQuery底层采用的是词条匹配查询
     */
    @Test
    public void testMatchQuery(){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "地方"));
        // 搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 总条数
        long total = items.getTotalElements();
        System.out.println("total = " + total);
        for (Item item : items) {
            System.out.println(item);
        }
    }
```
* NativeSearchQueryBuilder：Spring提供的一个查询条件构建器，帮助构建json格式的请求体
* QueryBuilders.matchQuery(“title”, “地方”)：利用QueryBuilders来生成一个查询。QueryBuilders提供了大量的静态方法，用于生成各种不同类型的查询，和ES的http请求中的查询方式一一对应
* Page<item>：默认是分页查询，因此返回的是一个分页的结果对象，包含属性：
![](https://github.com/lk6678979/image/blob/master/es-3.jpg)
	
### 5.4 测试代码
```java
/**
     *
     *@Description:matchQuery
     */
    @Test
    public void testMathQuery(){
        // 创建对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 在queryBuilder对象中自定义查询
        //matchQuery:底层就是使用的termQuery
        queryBuilder.withQuery(QueryBuilders.matchQuery("title","坚果"));
        //查询，search 默认就是分页查找
        Page<Item> page = this.itemRepository.search(queryBuilder.build());
        //获取数据
        long totalElements = page.getTotalElements();
        System.out.println("获取的总条数:"+totalElements);

        for(Item item:page){
            System.out.println(item);
        }
    }


    /**
     * @Description:
     * termQuery:功能更强大，除了匹配字符串以外，还可以匹配
     * int/long/double/float/....		
     */
    @Test
    public void testTermQuery(){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(QueryBuilders.termQuery("price",998.0));
        // 查找
        Page<Item> page = this.itemRepository.search(builder.build());

        for(Item item:page){
            System.out.println(item);
        }
    }
    
    /**
     * @Description:布尔查询		
     */
    @Test
    public void testBooleanQuery(){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        builder.withQuery(
                QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("title","华为"))
                                         .must(QueryBuilders.matchQuery("brand","华为"))
        );

        // 查找
        Page<Item> page = this.itemRepository.search(builder.build());
        for(Item item:page){
            System.out.println(item);
        }
    }

	/**
     * @Description:模糊查询		
     */
    @Test
    public void testFuzzyQuery(){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(QueryBuilders.fuzzyQuery("title","faceoooo"));
        Page<Item> page = this.itemRepository.search(builder.build());
        for(Item item:page){
            System.out.println(item);
        }

    }
```
### 5.5 分页查询
利用NativeSearchQueryBuilder可以方便的实现分页(可以发现，Elasticsearch中的分页是从第0页开始)：
```java
/**
     * @Description:分页查询		
     */
	@Test
	public void searchByPage(){
	    // 构建查询条件
	    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
	    // 添加基本分词查询
	    queryBuilder.withQuery(QueryBuilders.termQuery("title", "地方"));
	    // 分页：
	    int page = 0;
	    int size = 2;
	    queryBuilder.withPageable(PageRequest.of(page,size));
	
	    // 搜索，获取结果
	    Page<Item> items = this.itemRepository.search(queryBuilder.build());
	    // 总条数
	    long total = items.getTotalElements();
	    System.out.println("总条数 = " + total);
	    // 总页数
	    System.out.println("总页数 = " + items.getTotalPages());
	    // 当前页
	    System.out.println("当前页：" + items.getNumber());
	    // 每页大小
	    System.out.println("每页大小：" + items.getSize());
	
	    for (Item item : items) {
	        System.out.println(item);
	    }
	}
```
### 5.6 排序
排序也通用通过NativeSearchQueryBuilder完成：
```java
/**
     * @Description:排序查询	
     */
	@Test
	public void searchAndSort(){
	    // 构建查询条件
	    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
	    // 添加基本分词查询
            queryBuilder.withQuery(QueryBuilders.termQuery("title", "地方"));
	
	    // 排序
	    queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
	
	    // 搜索，获取结果
	    Page<Item> items = this.itemRepository.search(queryBuilder.build());
	    // 总条数
	    long total = items.getTotalElements();
	    System.out.println("总条数 = " + total);
	
	    for (Item item : items) {
	        System.out.println(item);
	    }
	}
```
## 6. 聚合
聚合可以让我们极其方便的实现对数据的统计、分析。例如：

什么品牌的手机最受欢迎？
这些手机的平均价格、最高价格、最低价格？
这些手机每月的销售情况如何？
实现这些统计功能的比数据库的sql要方便的多，而且查询速度非常快，可以实现近实时搜索效果。

### 6.1 聚合基本概念
Elasticsearch中的聚合，包含多种类型，最常用的两种，一个叫桶（bucket），一个叫度量（metrics）：  
#### 6.1.1 桶（bucket）
桶的作用，是按照某种方式对数据进行分组，每一组数据在ES中称为一个桶，例如我们根据职能对人划分，可以得到售前桶、研发桶，测试桶……或者我们按照年龄段对人进行划分:[0,10),[10,20),[20,30),[30,40)等。  
Elasticsearch中提供的划分桶的方式有很多:   
* Date Histogram Aggregation：根据日期阶梯分组，例如给定阶梯为周，会自动每周分为一组
* Histogram Aggregation：根据数值阶梯分组，与日期类似
* Terms Aggregation：根据词条内容分组，词条内容完全匹配的为一组
* Range Aggregation：数值和日期的范围分组，指定开始和结束，然后按段分组
* ……  
综上所述，我们发现bucket aggregations 只负责对数据进行分组，并不进行计算，因此往往bucket中往往会嵌套另一种聚合：metrics aggregations即度量
#### 6.1.2 度量（metrics）
分组完成以后，我们一般会对组中的数据进行聚合运算，例如求平均值、最大、最小、求和等，这些在ES中称为度量  
比较常用的一些度量聚合方式：  
* Avg Aggregation：求平均值
* Max Aggregation：求最大值
* Min Aggregation：求最小值
* Percentiles Aggregation：求百分比
* Stats Aggregation：同时返回avg、max、min、sum、count等
* Sum Aggregation：求和
* Top hits Aggregation：求前几
* Value Count Aggregation：求总数
* ……  
注意：在ES中，需要进行聚合、排序、过滤的字段其处理方式比较特殊，因此不能被分词。这里我们将color和make这两个文字类型的字段设置为keyword类型，这个类型不会被分词，将来就可以参与聚合
### 6.2 聚合为桶
桶就是分组，比如这里我们按照品牌brand进行分组：
```java
/**
     * @Description:按照品牌brand进行分组		
     */
	@Test
	public void testAgg(){
	    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
	    // 不查询任何结果
	    queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
	    // 1、添加一个新的聚合，聚合类型为terms（根据词条内容分组，词条内容完全匹配的为一组），聚合名称为brands，聚合字段为brand
	    queryBuilder.addAggregation(
	        AggregationBuilders.terms("brands").field("brand"));
	    // 2、查询,需要把结果强转为AggregatedPage类型
	    AggregatedPage<Item> aggPage = (AggregatedPage<Item>) this.itemRepository.search(queryBuilder.build());
	    // 3、解析
	    // 3.1、从结果中取出名为brands的那个聚合，
	    // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
	    StringTerms agg = (StringTerms) aggPage.getAggregation("brands");
	    // 3.2、获取桶
	    List<StringTerms.Bucket> buckets = agg.getBuckets();
	    // 3.3、遍历
	    for (StringTerms.Bucket bucket : buckets) {
	        // 3.4、获取桶中的key，即品牌名称
	        System.out.println(bucket.getKeyAsString());
	        // 3.5、获取桶中的文档数量
	        System.out.println(bucket.getDocCount());
	    }
	
	}
```
关键API
* AggregationBuilders：聚合的构建工厂类。所有聚合都由这个类来构建，看看他的静态方法：
![](https://github.com/lk6678979/image/blob/master/es-4.jpg)
```java
（1）统计某个字段的数量
  ValueCountBuilder vcb=  AggregationBuilders.count("count_uid").field("uid");
（2）去重统计某个字段的数量（有少量误差）
 CardinalityBuilder cb= AggregationBuilders.cardinality("distinct_count_uid").field("uid");
（3）聚合过滤
FilterAggregationBuilder fab= AggregationBuilders.filter("uid_filter").filter(QueryBuilders.queryStringQuery("uid:001"));
（4）按某个字段分组
TermsBuilder tb=  AggregationBuilders.terms("group_name").field("name");
（5）求和
SumBuilder  sumBuilder=	AggregationBuilders.sum("sum_price").field("price");
（6）求平均
AvgBuilder ab= AggregationBuilders.avg("avg_price").field("price");
（7）求最大值
MaxBuilder mb= AggregationBuilders.max("max_price").field("price"); 
（8）求最小值
MinBuilder min=	AggregationBuilders.min("min_price").field("price");
（9）按日期间隔分组
DateHistogramBuilder dhb= AggregationBuilders.dateHistogram("dh").field("date");
（10）获取聚合里面的结果
TopHitsBuilder thb=  AggregationBuilders.topHits("top_result");
（11）嵌套的聚合
NestedBuilder nb= AggregationBuilders.nested("negsted_path").path("quests");
（12）反转嵌套
AggregationBuilders.reverseNested("res_negsted").path("kps ");
```
* AggregatedPage：聚合查询的结果类。它是Page<T>的子接口,AggregatedPage在Page功能的基础上，拓展了与聚合相关的功能，它其实就是对聚合结果的一种封装：
```java
public interface AggregatedPage<T> extends FacetedPage<T>, ScrolledPage<T> {
    boolean hasAggregations();

    Aggregations getAggregations();

    Aggregation getAggregation(String var1);
}
```
而返回的结果都是Aggregation类型对象，不过根据字段类型不同，又有不同的子类表示
### 6.3 嵌套聚合，求平均值
```java
/**
     * @Description:嵌套聚合，求平均值		
     */
	@Test
	public void testSubAgg(){
	    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
	    // 不查询任何结果
	    queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
	    // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
	    queryBuilder.addAggregation(
	        AggregationBuilders.terms("brands").field("brand")
	        .subAggregation(AggregationBuilders.avg("priceAvg").field("price")) // 在品牌聚合桶内进行嵌套聚合，求平均值
	    );
	    // 2、查询,需要把结果强转为AggregatedPage类型
	    AggregatedPage<Item> aggPage = (AggregatedPage<Item>) this.itemRepository.search(queryBuilder.build());
	    // 3、解析
	    // 3.1、从结果中取出名为brands的那个聚合，
	    // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
	    StringTerms agg = (StringTerms) aggPage.getAggregation("brands");
	    // 3.2、获取桶
	    List<StringTerms.Bucket> buckets = agg.getBuckets();
	    // 3.3、遍历
	    for (StringTerms.Bucket bucket : buckets) {
	        // 3.4、获取桶中的key，即品牌名称  3.5、获取桶中的文档数量
	        System.out.println(bucket.getKeyAsString() + "，共" + bucket.getDocCount() + "台");
	
	        // 3.6.获取子聚合结果：
	        InternalAvg avg = (InternalAvg) bucket.getAggregations().asMap().get("priceAvg");
	        System.out.println("平均售价：" + avg.getValue());
	    }
	
	}
```

