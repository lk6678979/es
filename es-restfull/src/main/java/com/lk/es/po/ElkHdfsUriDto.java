package com.lk.es.po;

import lombok.Data;

import java.util.List;

/**
 * @描述:
 * @公司:
 * @作者: 刘恺
 * @版本: 1.0.0
 * @日期: 2019-08-15 19:37:05
 */
@Data
public class ElkHdfsUriDto {
    String uriStr;
    List<ElkDto> fileContent;
    String vin;
    String vinType;
    String year;
    String month;
    String day;
}
