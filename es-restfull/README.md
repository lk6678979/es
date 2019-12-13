# Sprinboot2.0.3整合Elasticsearch(基于REST FULL）

## 1. 官方API
https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-supported-apis.html

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
        <elasticsearch.version>7.3.1</elasticsearch.version>
        <commons-lang3.version>3.5</commons-lang3.version>
    </properties>

   <dependencies>
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
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>${elasticsearch.version}</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch</groupId>
            <artifactId>elasticsearch</artifactId>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-client</artifactId>
            <version>${elasticsearch.version}</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-client-sniffer</artifactId>
            <version>${elasticsearch.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>${commons-lang3.version}</version>
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
  elasticsearch:
    rest:
      #es节点，都好分割多个节点，端口号9200
      cluster-nodes: elk01:9200,elk02:9200,elk03:9200
      #连接超时是时间
      connect-timeout: 5000
      #套接字连接超时是时间
      socket-timeout: 60000
      #异步请求线程数
      io-thread-count: 1
```
### 2.2 配置类编写
```java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
public class ElasticsearchRestClientConfig {

    /**
     * 使用冒号隔开ip和端口1
     */
    @Value("${spring.elasticsearch.rest.cluster-nodes}")
    private String[] ipAddress;

    /**
     * 连接超时是时间
     */
    @Value("${spring.elasticsearch.rest.connect-timeout}")
    private int connectTimeout;

    /**
     * 套接字连接超时是时间
     */
    @Value("${spring.elasticsearch.rest.socket-timeout}")
    private int socketTimeout;

    /**
     * 异步请求线程数
     */
    @Value("${spring.elasticsearch.rest.io-thread-count}")
    private int ioThreadCount;

    private static final int ES_IPADDRS_LENGTH = 2;
    private static final String HTTP_SCHEME = "http";

    @Bean
    public RestClientBuilder restClientBuilder() {
        HttpHost[] hosts = Arrays.stream(ipAddress)
                .map(this::makeHttpHost)
                .filter(Objects::nonNull)
                .toArray(HttpHost[]::new);
        RestClientBuilder clientBuilder = RestClient.builder(hosts);
        // 1.设置请求头
        Header[] defaultHeaders = {new BasicHeader("header", "value")};
        clientBuilder.setDefaultHeaders(defaultHeaders);
        /**
         *3.设置失败监听器，
         *每次节点失败都可以监听到，可以作额外处理
         */
        clientBuilder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                super.onFailure(node);
                System.out.println(node.getName() + "==节点失败了");
            }
        });

        /** 4.配置节点选择器，客户端以循环方式将每个请求发送到每一个配置的节点上，
         *发送请求的节点，用于过滤客户端，将请求发送到这些客户端节点，默认向每个配置节点发送，
         *这个配置通常是用户在启用嗅探时向专用主节点发送请求（即只有专用的主节点应该被HTTP请求命中）
         */
        clientBuilder.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);

        /**5. 配置异步请求的线程数量，Apache Http Async Client默认启动一个调度程序线程，以及由连接管理器使用的许多工作线程
         *（与本地检测到的处理器数量一样多，取决于Runtime.getRuntime().availableProcessors()返回的数量）。线程数可以修改如下,
         *这里是修改为1个线程，即默认情况
         */
        clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                return httpAsyncClientBuilder.setDefaultIOReactorConfig(
                        IOReactorConfig.custom().setIoThreadCount(ioThreadCount).build()
                );
            }
        });
        /**
         *6. 配置连接超时和套接字超时
         *配置请求超时，将连接超时（默认为1秒）和套接字超时（默认为30秒）增加，
         */
        clientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                // 连接5秒超时，套接字连接60s超时
                return requestConfigBuilder.setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout);
            }
        });
        return clientBuilder;
    }

    @Bean(name = "highLevelClient")
    public RestHighLevelClient highLevelClient(RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }


    private HttpHost makeHttpHost(String s) {
        assert StringUtils.isNotEmpty(s);
        String[] address = s.split(":");
        if (address.length == ES_IPADDRS_LENGTH) {
            String ip = address[0];
            int port = Integer.parseInt(address[1]);
            return new HttpHost(ip, port, HTTP_SCHEME);
        }
        return null;
    }

    @Bean
    public RestClient getClient(RestClientBuilder restClientBuilder) {
        return restClientBuilder.build();
    }
}
```

### 2.3 配置通信加密
```java
/*
配置通信加密，有多种方式：setSSLContext、setSSLSessionStrategy和setConnectionManager(它们的重要性逐渐递增)
    */
KeyStore truststore = KeyStore.getInstance("jks");
try (InputStream is = Files.newInputStream(keyStorePath)) {
    truststore.load(is, keyStorePass.toCharArray());
}
SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
final SSLContext sslContext = sslBuilder.build();
clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        return httpClientBuilder.setSSLContext(sslContext);
    }
});
```

### 2.4 RestClientBuilder中的自定义节点选择器的需求配置（–>大数据中是要求配置机架感知的）
```java
// 进行详细的配置
clientBuilder.setNodeSelector(new NodeSelector() {
    // 设置分配感知节点选择器，允许选择本地机架中的节点（如果有），否则转到任何机架中的任何其他节点。
    @Override
    public void select(Iterable<Node> nodes) {
        boolean foundOne = false;
        for (Node node: nodes) {
            String rackId = node.getAttributes().get("rack_id").get(0);
            if ("rack_one".equals(rackId)) {
                foundOne = true;
                break;
            }
        }
        if (foundOne) {
            Iterator<Node> nodesIt = nodes.iterator();
            while (nodesIt.hasNext()) {
                Node node = nodesIt.next();
                String rackId = node.getAttributes().get("rack_id").get(0);
                if ("rack_one".equals(rackId) == false) {
                    nodesIt.remove();
                }
            }
        }
    }
});
```

### 2.5 配置ES安全认证（如果你的ES集群设置了密码的话，或者密钥的话）
```java
/*
如果ES设置了密码，那这里也提供了一个基本的认证机制，下面设置了ES需要基本身份验证的默认凭据提供程序
    */
final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials("user", "password"));
clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }
});
/*
上面采用异步机制实现抢先认证，这个功能也可以禁用，这意味着每个请求都将在没有授权标头的情况下发送，然后查看它是否被接受，
并且在收到HTTP 401响应后，它再使用基本认证头重新发送完全相同的请求，这个可能是基于安全、性能的考虑
    */
clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        // 禁用抢先认证的方式
        httpClientBuilder.disableAuthCaching();
        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }
});
```

### 2.6 创建索引
```java
 @Autowired
    @Qualifier("highLevelClient")
    private RestHighLevelClient client;

    /**
     * 创建所以（不会创建字段映射）
     */
    @Test
    public void createIndex() {
        String index = "item44123444";
        //index名必须全小写，否则报错
        CreateIndexRequest request = new CreateIndexRequest(index);
        buildSetting(request);
        buildIndexMapping(request);
        request.alias(new Alias(index + "alias"));//设置别名
        request.setTimeout(TimeValue.timeValueMinutes(2));//设置创建索引超时2分钟
        // 同步请求(亲测可以)
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            // 处理响应
            boolean acknowledged = createIndexResponse.isAcknowledged();
            boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
            System.out.println(acknowledged + "," + shardsAcknowledged);
            client.close();
        } catch (IOException e) {
            log.error("索引{}创建异常:" + e.getMessage(), index);
        }
//异步请求 (自己测试)
/** ActionListener<CreateIndexResponse> listener = new ActionListener<CreateIndexResponse>() {
@Override public void onResponse(CreateIndexResponse createIndexResponse) {
boolean acknowledged = createIndexResponse.isAcknowledged();
boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
}
@Override public void onFailure(Exception e) {
e.printStackTrace();
}
};
 client.indices().createAsync(request, listener);*/
//        return true;
    }


    //设置分片
    public void buildSetting(CreateIndexRequest request) {
        request.settings(Settings.builder().put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2));
    }

    //设置index的mapping
    //elasticsearch7默认不在支持指定索引类型，默认索引类型是_doc，如果想改变，则配置include_type_name: true 即可
    public void buildIndexMapping(CreateIndexRequest request) {
        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> number = new HashMap<>();
        number.put("type", "text");
        properties.put("number", number);

        Map<String, Object> price = new HashMap<>();
        price.put("type", "float");
        properties.put("price", price);

        Map<String, Object> title = new HashMap<>();
        title.put("type", "text");
        properties.put("title", title);

        Map<String, Object> province = new HashMap<>();
        province.put("type", "text");
        properties.put("province", province);

        Map<String, Object> publishTime = new HashMap<>();
        publishTime.put("type", "text");
        properties.put("publishTime", publishTime);

        Map<String, Object> book = new HashMap<>();
        book.put("properties", properties);

        //map的底层必须只有一个，book对象，只有一个properties的元素，必须是properties
        request.mapping(book);
    }
```

### 2.7 查询索引
```
 @Test
    public void getInfo() throws IOException {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        MatchQueryBuilder pathMatch = QueryBuilders.matchQuery("path", "baseError.log");
        MatchQueryBuilder levelMatch = QueryBuilders.matchQuery("level", "ERROR");//这里可以根据字段进行搜索，must表示符合条件的，相反的mustnot表示不符合条件的
        MatchQueryBuilder messageMatch = QueryBuilders.matchQuery("message", "E1000:");
        // RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("fields_timestamp"); //新建range条件
        // rangeQueryBuilder.gte("2019-03-21T08:24:37.873Z"); //开始时间
        // rangeQueryBuilder.lte("2019-03-21T08:24:37.873Z"); //结束时间
        // boolBuilder.must(rangeQueryBuilder);
        boolBuilder.must(pathMatch).must(levelMatch).must(messageMatch);
        sourceBuilder.query(boolBuilder); //设置查询，可以是任何类型的QueryBuilder。
        sourceBuilder.from(0); //设置确定结果要从哪个索引开始搜索的from选项，默认为0
        sourceBuilder.size(10000); //设置确定搜素命中返回数的size选项，默认为10
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //设置一个可选的超时，控制允许搜索的时间。
//        sourceBuilder.fetchSource(new String[]{"fields.port", "fields.entity_id", "fields.message"}, new String[]{}); //第一个是获取字段，第二个是过滤的字段，默认获取全部
        SearchRequest searchRequest = new SearchRequest("data-synchro-*"); //索引
        searchRequest.types("logs"); //类型
        searchRequest.source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits searchHits = response.getHits();  //SearchHits提供有关所有匹配的全局信息，例如总命中数或最高分数：
        SearchHit[] hits = searchHits.getHits();
        //无数据
        if (hits == null || hits.length == 0) {
            return;
        }
        List<Map> result = new ArrayList<>();
        for (SearchHit item : hits) {
            String _id = item.getId();//ID
            String _index = item.getIndex();//索引
            Map<String, Object> _source = item.getSourceAsMap();//数据
            //写入hdfs的文件内容
            String hdfsFileStr = _source.get("message").toString().replace("E1000:", "");
            System.out.println(hdfsFileStr);
            String timestamp = hdfsFileStr.split("\\|", -1)[0];
            Calendar timestampC = Calendar.getInstance();
            timestampC.setTime(new Date(Long.parseLong(timestamp)));
            //写入hdfs的文件路径
            //TODO 将数据写到HDFS并且删除数据
//                    elasticsearchTemplate.delete(_index, "logs", _id);
        }
        return;
    }
```
上面的ElasticSearchService 有几个常用的Builder类和方法需要了解下。
* BoolQueryBuilder 用来拼装查询条件。一般来说must的性能要低一些，因为他要进行打分评估，也就是说要进行_score，而filter则不会。
```java
filter(QueryBuilder queryBuilder)

Adds a query that must appear in the matching documents but will not contribute to scoring.
 
must(QueryBuilder queryBuilder)
Adds a query that must appear in the matching documents and will contribute to scoring.
 
should(QueryBuilder queryBuilder)
Adds a query that should appear in the matching documents.
```
简单来说：must 相当于 与 & = ；must not 相当于 非 ~   ！=；should 相当于 或  |   or ；filter  过滤
```java
//filter 效率比 must高的多 
 if (StringUtils.isNotBlank(query.getRouterDatabaseNo())) {
　　　boolQueryBuilder.filter(QueryBuilders.termQuery("routerDatabaseNo", query.getRouterDatabaseNo()));
 }

//时间段 一定要有头有尾 不然会出现慢查询
 if (null != query.getCreateTime() && null != query.getUpdateTime()) {
     boolQueryBuilder.filter(QueryBuilders.rangeQuery("createTime").from( query.getCreateTime()).to(query.getUpdateTime()));
 }
```
* SearchSourceBuilder
```java
SearchRequest searchRequest = new SearchRequest("customer"); //将请求限制为一个索引
 SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
 searchRequest.types("doc"); //将请求限制为一个类型。
 searchRequest.source(sourceBuilder); //将SearchSourceBuilder添加到SeachRequest
```
创建SeachRequest，没有参数，这将针对所有索引运行。有参数，则按参数所传值为索引，此处“customer”为索引值。大多数搜索参数都添加到SearchSourceBuilder中，它为搜索请求body中的所有内容提供了setter。

SearchSourceBuilder的排序：允许添加一个或多个SortBuilder实例，有四种特殊的实现（Field-，Score-，GeoDistance-和ScriptSortBuilder）。
```java
sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC)); //按_score降序排序（默认值）。
sourceBuilder.sort(new FieldSortBuilder("_uid").order(SortOrder.ASC));//也可以按_id字段进行升序排序
```
SearchSourceBuilder的源过滤：默认情况下，搜索请求会返回文档_source的内容，但与Rest API中的内容一样，你可以覆盖此行为，例如，你可以完全关闭_source检索：
```java
sourceBuilder.fetchSource(false);
```
该方法还接受一个或多个通配符模式的数组，以控制以更精细的方式包含或排除哪些字段：
```java
String[] includeFields = new String[] {"title", "user", "innerObject.*"};
String[] excludeFields = new String[] {"_type"};
sourceBuilder.fetchSource(includeFields, excludeFields);
```
SearchSourceBuilder的请求结果高亮显示：通过在SearchSourceBuilder上设置HighlightBuilder，可以实现高亮搜索结果，通过将一个或多个HighlightBuilder.Field实例添加到HighlightBuilder，可以为每个字段定义不同的高亮行为。
```java
SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
HighlightBuilder highlightBuilder = new HighlightBuilder(); //创建一个新的HighlightBuilder。
HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("title");  //为title字段创建字段高光色。
highlightTitle.highlighterType("unified"); // 设置字段高光色类型。
highlightBuilder.field(highlightTitle);   //将字段高光色添加到高亮构建器。
HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("user");
highlightBuilder.field(highlightUser);
searchSourceBuilder.highlighter(highlightBuilder);
```
### 2.8 添加数据
```java
public void putData(Book book) throws IOException {
        IndexRequest indexRequest = new IndexRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, book.getNumber());
        ObjectMapper mapper = new ObjectMapper();
        byte[] json = mapper.writeValueAsBytes(book);
        indexRequest.source(json, XContentType.JSON);
        client.index(indexRequest);
    }
```
### 2.9 更新数据
```java
 public void updateData(Book book) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, book.getNumber());
        updateRequest.doc(createIndexRequest(book));
        GetResult getResult =
                client.update(updateRequest).getGetResult();
    }

    private IndexRequest createIndexRequest(Book book) throws JsonProcessingException {
        IndexRequest indexRequest = new IndexRequest(EsConsts.INDEX_NAME, EsConsts.TYPE, book.getNumber());
        ObjectMapper mapper = new ObjectMapper();
        byte[] json = mapper.writeValueAsBytes(book);
        indexRequest.source(json, XContentType.JSON);
        return indexRequest;
    }
```
注：es的更新数据，不论是直接用script方式，还是updaterequest.doc方式，貌似都是在原来已有的数据上合并（涉及到的字段更新，update中未涉及到的字段保持不变）；如果需要全量覆盖，直接用添加数据请求。
### 2.10 删除数据
```java
public String deleteData(String id) throws IOException{
        DeleteRequest deleteRequest = new DeleteRequest(EsConsts.INDEX_NAME, EsConsts.TYPE,
                id);
        DeleteResponse deleteResponse =  client.delete(deleteRequest);
        return deleteResponse.getResult().toString();
    }
```


