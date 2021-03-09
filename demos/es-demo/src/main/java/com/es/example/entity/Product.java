package com.es.example.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 搜索商品的信息
 *
 * @author Richard
 */
@Entity
@Table(name = "pms_product")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity implements Serializable {

	private String productSn;

	private Long brandId;

	private String brandName;

	private Long productCategoryId;

	private String productCategoryName;

	private String name;

	private String subTitle;

	private String keywords;

	private BigDecimal price;

	private Integer sale;

	private Integer newStatus;

	private Integer recommandStatus;

	private Integer stock;

	private Integer promotionType;

	private Integer sort;

//	private List<EsProductAttributeValue> attrValueList;

}
