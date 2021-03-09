package com.jd.entity;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.config.BigDecimalSerializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @projectName: jd-search-api
 * @className: com.jd.entity.GoodsDetail
 * @description: 商品详情实体类
 * @author: tong.li
 * @createTime: 2020/12/8 19:14
 * @version: v1.0
 * @copyright: 版权所有 © 李彤
 */
@Data
public class GoodsDetail implements Serializable {

    private static final long serialVersionUID = 121384327857834L;

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品SKU标识
     */
    private String sku;

    /**
     * 商品名称
     */
    private String title;

    /**
     * 商品图片
     */
    private String imgUrl;

    /**
     * 商品价格
     */
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal price;

    /**
     * 商品所在的商户名称
     */
    private String shopName;

    /**
     * 评价数
     */
    private Integer evaluationCount;

    /**
     * 成交数，随机生成
     */
    private Integer transactionsCount;

    /**
     * 商品详情页跳转地址
     */
    private String detailUrl;


    /**
     * 搜索排名
     */
    private float score;

}
