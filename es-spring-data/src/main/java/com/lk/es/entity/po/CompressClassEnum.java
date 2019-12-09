package com.lk.es.entity.po;

/**
 * 
 * @描述: X9E和HDFS的企标msgType转换枚举类
 * @公司:
 * @作者: 刘恺
 * @版本: 1.0.0
 * @日期: 2019年04月21日 下午7:14:50
 */
public enum CompressClassEnum {

	GZIP(SynchronizationConstants.HDFS_COMPRESS_CLASS_GZIP,SynchronizationConstants.HDFS_COMPRESS_CLASS_GZIP_SUFFIX),
	BZip2(SynchronizationConstants.HDFS_COMPRESS_CLASS_BZIP2,SynchronizationConstants.HDFS_COMPRESS_CLASS_BZIP2_SUFFIX),
	SNAPPY(SynchronizationConstants.HDFS_COMPRESS_CLASS_SNAPPY,SynchronizationConstants.HDFS_COMPRESS_CLASS_SNAPPY_SUFFIX),
	LZ4(SynchronizationConstants.HDFS_COMPRESS_CLASS_LZ4,SynchronizationConstants.HDFS_COMPRESS_CLASS_LZ4_SUFFIX),
	DEFLATE(SynchronizationConstants.HDFS_COMPRESS_CLASS_DEFLATE,SynchronizationConstants.HDFS_COMPRESS_CLASS_DEFLATE_SUFFIX),
	;
	    private final String compressClass;
	    private final String fileSuffix;

	    private CompressClassEnum(String compressClass, String fileSuffix){
	        this.compressClass = compressClass;
	        this.fileSuffix = fileSuffix;
	    }
	    //根据key获取枚举
	    public static CompressClassEnum getEnumByKey(String compressClass){
	        if(null == compressClass){
	            return null;
	        }
	        for(CompressClassEnum temp: CompressClassEnum.values()){
	            if(temp.getCompressClass().equals(compressClass)){
	                return temp;
	            }
	        }
	        return null;
	    }
	    public String getCompressClass() {
	        return compressClass;
	    }
	    public String getFileSuffix() {
	        return fileSuffix;
	    }
}
