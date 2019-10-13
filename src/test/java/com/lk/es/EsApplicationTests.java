package com.lk.es;

import com.lk.es.entity.po.Item;
import com.lk.es.repository.ItemRepository;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
public class EsApplicationTests {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemRepository itemRepository;


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


    @Test
    public void query() {
        Iterable<Item> list = this.itemRepository.findAll(Sort.by("price").ascending());

        for (Item item:list){
            System.out.println(item);
        }
    }
}
