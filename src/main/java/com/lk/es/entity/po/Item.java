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
 *   ·text：存储数据时候，会自动分词，并生成索引
 *   ·keyword：存储数据时候，不会分词建立索引
 *   ·Numerical：数值类型，分两类
 *     ·基本数据类型：long、interger、short、byte、double、float、half_float
 *     ·浮点数的高精度类型：scaled_float,需要指定一个精度因子，比如10或100。elasticsearch会把真实值乘以这个因子后存储，取出时再还原
 *   ·Date：日期类型,elasticsearch可以对日期格式化为字符串存储，但是建议我们存储为毫秒值，存储为long，节省空间
 * ·index：是否索引，布尔类型，默认是true，ES6.3 index属性支持false和true,false不能搜索相当于no,true可以索引。默认使用standar分词，index属性的取值有三个:
 *   ·analyzed:字段被索引，会做分词，可搜索。反过来，如果需要根据某个字段进搜索，index属性就应该设置为analyzed。
 *   ·not_analyzed：字段值不分词，会被原样写入索引。反过来，如果某些字段需要完全匹配，比如人名、地名，index属性设置为not_analyzed为佳。
 *   ·no:字段不写入索引，当然也就不能搜索。反过来，有些业务要求某些字段不能被搜索，那么index属性设置为no即可。
 * ·store：是否存储，布尔类型，默认是false
 * ·analyzer：分词器名称，这里的ik_max_word即使用ik分词器
 */
@Data
@Document(indexName = "item", type = "docs", shards = 1, replicas = 0)
public class Item {
    /**
     * 主键ID
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
