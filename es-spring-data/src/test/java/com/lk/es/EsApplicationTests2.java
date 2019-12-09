package com.lk.es;

import com.lk.es.entity.po.CompressClassEnum;
import com.lk.es.entity.po.ElkHdfsUriDto;
import org.apache.http.client.utils.DateUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
public class EsApplicationTests2 {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * HDFS压缩方式class
     */
    @Value("${business.hdfsCompressClass:''}")
    private String hdfsCompressClass;

    /**
     * HDFS压缩是否需要压缩
     */
    @Value("${business.hdfsNeedCompress:1}")
    private String hdfsNeedCompress;

    @Test
    public void test() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withIndices("real-time-monitor-*");
        queryBuilder.withTypes("logs");
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("level", "ERROR"));
        queryBuilder.withQuery(QueryBuilders.matchQuery("message", "E0004:"));
        // 排序
        queryBuilder.withSort(SortBuilders.fieldSort("@timestamp").order(SortOrder.ASC));
        // 分页：
        int page = 0;
        int size = 200;
        queryBuilder.withPageable(PageRequest.of(page, size));
        // 搜索，获取结果
        Page<HashMap> items = elasticsearchTemplate.queryForPage(queryBuilder.build(), HashMap.class);
        // 总条数
        long total = items.getTotalElements();
        System.out.println("总条数 = " + total);
        // 总页数
        System.out.println("总页数 = " + items.getTotalPages());
        // 当前页
        System.out.println("当前页：" + items.getNumber());
        // 每页大小
        System.out.println("每页大小：" + items.getSize());

        for (Map item : items) {
            System.out.println(item);
        }
    }

    @Test
    public void test2() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withIndices("data-synchro-*");
        queryBuilder.withTypes("logs");
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("level", "ERROR"));
        queryBuilder.withQuery(QueryBuilders.matchQuery("message", "E1000:"));//数据同步E1000:
        // 排序
        queryBuilder.withSort(SortBuilders.fieldSort("@timestamp").order(SortOrder.ASC));
        // 分页：
        int page = 0;
        int size = 200;
        queryBuilder.withPageable(PageRequest.of(page, size));
        elasticsearchTemplate.query(queryBuilder.build(), new ResultsExtractor<Page<Map>>() {
            @Override
            public Page<Map> extract(SearchResponse searchResponse) {
                SearchHit[] hits = searchResponse.getHits().getHits();
                //无数据
                if (hits == null || hits.length == 0) {
                    return null;
                }
                List<Map> result = new ArrayList<>();
                for (SearchHit item : hits) {
                    String _id = item.getId();//ID
                    String _index = item.getIndex();//索引
                    Map<String, Object> _source = item.getSource();//数据
                    System.out.println(_source);
                    //写入hdfs的文件内容
                    String hdfsFileStr = _source.get("message").toString().replace("E1000:", "");
                    String timestamp = hdfsFileStr.split("\\|", -1)[0];
                    Calendar timestampC = Calendar.getInstance();
                    timestampC.setTime(new Date(Long.parseLong(timestamp)));
                    //写入hdfs的文件路径
                    //TODO 将数据写到HDFS并且删除数据
//                    elasticsearchTemplate.delete(_index, "logs", _id);
                }
                return null;
            }
        });
        // 搜索，获取结果
        Page<HashMap> items = elasticsearchTemplate.queryForPage(queryBuilder.build(), HashMap.class);
        // 总条数
        long total = items.getTotalElements();
        System.out.println("总条数 = " + total);
        // 总页数
        System.out.println("总页数 = " + items.getTotalPages());
        // 当前页
        System.out.println("当前页：" + items.getNumber());
        // 每页大小
        System.out.println("每页大小：" + items.getSize());

        for (Map item : items) {
            System.out.println(item);
        }
    }
}
