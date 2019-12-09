package com.lk.es.entity.po;

/**
 * @描述:
 * @公司:
 * @作者: 刘恺
 * @版本: 1.0.0
 * @日期: 2019-03-29 13:40:05
 */
public interface SynchronizationConstants {
    /**
     * REDIS中的key---VIN唯一ID生成序列
     */
    String REDIS_KEY_VINIDSEQ = "synchro:vinIdSeq";

    /**
     * REDIS中的key---VIN存储的zsort的key
     */
    String REDIS_KEY_VIN_ZSORT = "synchro:vinZSort";

    /**
     * REDIS中的key---VIN添加锁（保证数据只添加一次）
     */
    String REDIS_KEY_VIN_LOCK = "synchro:vinAddLock:";

    /**
     * REDIS中的key---VIN信号存储List(国标)
     */
    String REDIS_KEY_VIN_DATALIST = "synchro:vinDataList:";

    /**
     * REDIS中的key---VIN信号存储List(企标1S)
     */
    String REDIS_KEY_VIN_DATALIST_QB1S = "synchro:vinDataListQb1S:";

    /**
     * REDIS中的key---VIN信号存储List(企标5S)
     */
    String REDIS_KEY_VIN_DATALIST_QB5S = "synchro:vinDataListQb5S:";

    /**
     * REDIS中的key---VIN信号存储List(企标30S)
     */
    String REDIS_KEY_VIN_DATALIST_QB30S = "synchro:vinDataListQb30S:";

    /**
     * REDIS中的key---VIN信号存储List(企标60S)
     */
    String REDIS_KEY_VIN_DATALIST_QB60S = "synchro:vinDataListQb60S:";

    /**
     * REDIS中的key---VIN信号存储List(企标event)
     */
    String REDIS_KEY_VIN_DATALIST_QBEVENT = "synchro:vinDataListQbEvent:";

    /**
     * REDIS中的key---已删除数据检查队列
     */
    String REDIS_KEY_VIN_CHECHLIST = "synchro:vinCheckList:";

    /**
     * REDIS中的key---车辆最后上传的数据(国标)
     */
    String REDIS_KEY_VIN_LASTDATA = "synchro:vinLastData";

    /**
     * REDIS中的key---车辆最后上传的数据(企标1S)
     */
    String REDIS_KEY_VIN_LASTDATA_QB1S = "synchro:vinLastDataQb1S";

    /**
     * REDIS中的key---车辆最后上传的数据(企标5S)
     */
    String REDIS_KEY_VIN_LASTDATA_QB5S = "synchro:vinLastDataQb5S";

    /**
     * REDIS中的key---车辆最后上传的数据(企标30S)
     */
    String REDIS_KEY_VIN_LASTDATA_QB30S = "synchro:vinLastDataQb30S";

    /**
     * REDIS中的key---车辆最后上传的数据(企标60S)
     */
    String REDIS_KEY_VIN_LASTDATA_QB60S = "synchro:vinLastDataQb60S";

    /**
     * REDIS中的key---车辆最后上传的数据(企标EVENT)
     */
    String REDIS_KEY_VIN_LASTDATA_QBEVENT = "synchro:vinLastDataQbEvent";

    /**
     * kafka的header对应key-topic
     */
    String KAFKA_HEADER_TOPIC = "kafka_receivedTopic";

    /**
     * kafka的header对应key-partitionId
     */
    String KAFKA_HEADER_PARTITIONID = "kafka_receivedPartitionId";

    /**
     * kafka的header对应key-offset
     */
    String KAFKA_HEADER_OFFSET = "kafka_offset";

    /**
     * kafka中数据key--vin码
     */
    String KAFKA_DATA_KEY_VIN = "vin";

    /**
     * kafka中数据key--信号上传时间戳
     */
    String KAFKA_DATA_KEY_TIMESTAMP = "sampleTime";

    /**
     * kafka中数据key--车型
     */
    String KAFKA_DATA_KEY_VEHICLESERIES = "vehicleSeries";

    /**
     * 成都国标 数据key
     */
    String CD_KAFKA_DATA_KEY_CONTENT = "content";

    /**
     * 成都国标 数据key kafka中数据key--车型
     */
    String CD_KAFKA_DATA_KEY_VIN_TYPE = "vintype";
    /**
     *成都国标 kafka中数据key--vin码
     */
    String CD_KAFKA_DATA_KEY_VIN = "vin";

    String CD_KAFKA_DATA_KEY_MSG_TYPE = "type";
    String CD_KAFKA_DATA_KEY_TIMESTAMP = "@timestamp";
    String CD_KAFKA_DATA_KEY_CREATETIME = "createtime";
    /**
     * kafka中数据key--车辆信号数据集
     */
    String KAFKA_DATA_KEY_DATALIST = "dataList";

    /**
     * kafka中数据key--消息类型
     */
    String KAFKA_DATA_KEY_MSGTYPE = "msgType";

    /**
     * REDIS中的key---VIN信号最后一次同步成都Kafka处理时间(国标)
     */
    String REDIS_KEY_VIN_CDKAFKALASTDEALTIME = "synchro:cdDealTime:";

    /**
     * REDIS中的key---VIN信号最后一次同步广新Kafka处理时间(国标)
     */
    String REDIS_KEY_VIN_GXKAFKALASTDEALTIME = "synchro:gxDealTime:";

    /**
     * REDIS中的key---VIN信号最后一次同步广新Kafka处理时间(企标1)
     */
    String REDIS_KEY_VIN_GXKAFKALASTDEALTIMES_QB1S = "synchro:gxDealTimeQb1S:";

    /**
     * REDIS中的key---VIN信号最后一次同步广新Kafka处理时间(企标5S)
     */
    String REDIS_KEY_VIN_GXKAFKALASTDEALTIMES_QB5S = "synchro:gxDealTimeQb5S:";
    /**
     * REDIS中的key---VIN信号最后一次同步广新Kafka处理时间(企标3S)
     */
    String REDIS_KEY_VIN_GXKAFKALASTDEALTIMES_QB30S = "synchro:gxDealTimeQb30S:";
    /**
     * REDIS中的key---VIN信号最后一次同步广新Kafka处理时间(企标60S)
     */
    String REDIS_KEY_VIN_GXKAFKALASTDEALTIMES_QB60S = "synchro:gxDealTimeQb60S:";
    /**
     * REDIS中的key---VIN信号最后一次同步广新Kafka处理时间(企标EVENT)
     */
    String REDIS_KEY_VIN_GXKAFKALASTDEALTIMES_QBEVENT = "synchro:gxDealTimeQbEvent:";
    /**
     * REDIS中的key---VIN信号最后一次同步HDFS处理时间(国标)
     */
    String REDIS_KEY_VIN_HDFSLASTDEALTIME = "synchro:hDealTime:";

    /**
     * REDIS中的key---VIN信号最后一次同步HDFS处理时间(企标1S)
     */
    String REDIS_KEY_VIN_HDFSLASTDEALTIME_QB1S = "synchro:hDealTimeQb1S:";

    /**
     * REDIS中的key---VIN信号最后一次同步HDFS处理时间(企标5S)
     */
    String REDIS_KEY_VIN_HDFSLASTDEALTIME_QB5S = "synchro:hDealTimeQb5S:";

    /**
     * REDIS中的key---VIN信号最后一次同步HDFS处理时间(企标30S)
     */
    String REDIS_KEY_VIN_HDFSLASTDEALTIME_QB30S = "synchro:hDealTimeQb30S:";

    /**
     * REDIS中的key---VIN信号最后一次同步HDFS处理时间(企标60S)
     */
    String REDIS_KEY_VIN_HDFSLASTDEALTIME_QB60S = "synchro:hDealTimeQb60S:";

    /**
     * REDIS中的key---VIN信号最后一次同步HDFS处理时间(企标EVENT)
     */
    String REDIS_KEY_VIN_HDFSLASTDEALTIME_QBEVENT = "synchro:hDealTimeQbEvent:";

    /**
     * REDIS返回字符串类型null
     */
    String REDIS_STRING_NULL = "null";

    /**
     * 企标信号-数据类型-1S
     */
    String QB_MSGTYPE_TBOX_PERIOD_1S = "tbox_period_1s";

    /**
     * 企标信号-数据类型-5S
     */
    String QB_MSGTYPE_TBOX_PERIOD_5S = "tbox_period_5s";

    /**
     * 企标信号-数据类型-30S
     */
    String QB_MSGTYPE_TBOX_PERIOD_30S = "tbox_period_30s";

    /**
     * 企标信号-数据类型-1S
     */
    String QB_MSGTYPE_TBOX_PERIOD_60S = "tbox_period_60s";

    /**
     * 企标信号-数据类型-EVENT
     */
    String QB_MSGTYPE_TBOX_PERIOD_EVENT = "tbox_event_msg";

    /**
     * SpringBean的ID前缀
     */
    String BEAN_PRE_SIGNALSERIALIZESERVICE = "signalSerializeService_";

    /**
     * 压缩类-Gzip
     */
    String HDFS_COMPRESS_CLASS_GZIP = "org.apache.hadoop.io.compress.GzipCodec";

    /**
     * 压缩类-BZip2
     */
    String HDFS_COMPRESS_CLASS_BZIP2 = "class：org.apache.hadoop.io.compress.BZip2Codec";

    /**
     * 压缩类-SnappyCodec
     */
    String HDFS_COMPRESS_CLASS_SNAPPY = "org.apache.hadoop.io.compress.SnappyCodec";

    /**
     * 压缩类-Lz4
     */
    String HDFS_COMPRESS_CLASS_LZ4 = "org.apache.hadoop.io.compress.Lz4Codec";

    /**
     * 压缩类-LDeflateCodec
     */
    String HDFS_COMPRESS_CLASS_DEFLATE = "org.apache.hadoop.io.compress.DeflateCodec";

    /**
     * 压缩类-GZIP文件后缀
     */
    String HDFS_COMPRESS_CLASS_GZIP_SUFFIX = ".gz";

    /**
     * 压缩类-BZip2文件后缀
     */
    String HDFS_COMPRESS_CLASS_BZIP2_SUFFIX = ".bz2";

    /**
     * 压缩类-LZ4文件后缀
     */
    String HDFS_COMPRESS_CLASS_LZ4_SUFFIX = ".lz4";

    /**
     * 压缩类-SNAPPY文件后缀
     */
    String HDFS_COMPRESS_CLASS_SNAPPY_SUFFIX = ".snappy";

    /**
     * 压缩类-DEFLATE文件后缀
     */
    String HDFS_COMPRESS_CLASS_DEFLATE_SUFFIX = ".deflate";

    /**
     * HDFS数据企标标识位1S
     */
    String HDFS_QB_MSGTYPE_1S = "1";

    /**
     * HDFS数据企标标识位5S
     */
    String HDFS_QB_MSGTYPE_5S = "5";

    /**
     * HDFS数据企标标识位1S
     */
    String HDFS_QB_MSGTYPE_30S = "3";

    /**
     * HDFS数据企标标识位60S
     */
    String HDFS_QB_MSGTYPE_60S = "6";

    /**
     * HDFS数据企标标识位EVENT
     */
    String HDFS_QB_MSGTYPE_EVENT = "e";


    /**
     * 序列化工具类的 信号类型枚举值
     */
    String SIGNAL_SERIALIZE_QB_TYPE_1S="period01s";
    String SIGNAL_SERIALIZE_QB_TYPE_5S="period05s";
    String SIGNAL_SERIALIZE_QB_TYPE_30S="period30s";
    String SIGNAL_SERIALIZE_QB_TYPE_60S="period60s";
    String SIGNAL_SERIALIZE_QB_TYPE_QB_EVENT="qbEvent";
}
