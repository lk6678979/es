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


    @Test
    public void testCreateIndex() {
        elasticsearchTemplate.createIndex(Item.class);
        elasticsearchTemplate.putMapping(Item.class);
        elasticsearchTemplate.map
    }

    @Test
    public void deleteIndex() {
        elasticsearchTemplate.deleteIndex(Item.class);
    }

}
