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

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void update() {
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

        for (Item item : list) {
            System.out.println(item);
        }
    }
}
