package com.lk.es.rest;

import com.lk.es.EsApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
@Slf4j
public class EsApplicationTests {
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

    @Test
    public void getErrorUrl() throws IOException {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        MatchQueryBuilder pathMatch = QueryBuilders.matchQuery("path", "baseError.log");
        MatchQueryBuilder levelMatch = QueryBuilders.matchQuery("level", "ERROR");//这里可以根据字段进行搜索，must表示符合条件的，相反的mustnot表示不符合条件的
        MatchQueryBuilder messageMatch = QueryBuilders.matchQuery("stack_trace", "is not sufficiently replicated yet");
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
            String url = getUrl(String.valueOf(_source.get("stack_trace")));
            System.out.println(url);
            //TODO 根据index和id去redis确认该文件是否被删除过，如果删除过着直接删除es中这条数据，如果没有则继续往下走
            //已经被删除，直接删除Es中的数据,结束处理逻辑
            if (checkUrlHasDelete(_index, _id)) {
                DeleteRequest deleteRequest = new DeleteRequest(_index, "logs", _id);
                client.delete(deleteRequest, RequestOptions.DEFAULT);
                continue;
            }
            //TODO 根据URL删除HDFS破损文件、数据库中的文件目录，事务

            //TODO 删除HDFS目录后，需要在redis存储删除的es数据的目录，key：index_id
            //TODO 删除ES中这条数据
            //TODO 循环执行，一直到ES查不到需要删除的URL为止，设定循环次数，超过次数直接报警告知开发人员
        }
        return;
    }

    public void deleteDbUrl(){

    }
    /**
     * 验证文件是否已经被删除过，false：没有被删除，true：已经被删除
     */
    public Boolean checkUrlHasDelete(String _index, String _id) {
        String redisKey = "sziov:hpe:" + _index + "_" + _id;
        return false;
    }

    public String getUrl(String stackTrace) {
        if (StringUtils.isBlank(stackTrace) || !stackTrace.startsWith("org.apache.hadoop.ipc.RemoteException: append")) {
            return null;
        }
        String[] errorAppendInfo = stackTrace.split("is not sufficiently replicated yet", -1);
        if (errorAppendInfo.length < 2) {
            return null;
        }
        String[] urlInfos = errorAppendInfo[0].split("src=");
        if (urlInfos.length < 2) {
            return null;
        }
        return urlInfos[1];
    }
}