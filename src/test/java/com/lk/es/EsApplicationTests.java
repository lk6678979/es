package com.lk.es;

import com.lk.es.entity.po.Item;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
public class EsApplicationTests {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /**
     * 创建所以（不会创建字段映射）
     */
    @Test
    public void createIndex() {
        elasticsearchTemplate.createIndex(Item.class);
    }


    /**
     * 给索引映射字段属性
     */
    @Test
    public void putMapping() {
        elasticsearchTemplate.putMapping(Item.class);
    }

    /**
     * 删除索引
     */
    @Test
    public void deleteIndex() {
        elasticsearchTemplate.deleteIndex(Item.class);
    }

}
