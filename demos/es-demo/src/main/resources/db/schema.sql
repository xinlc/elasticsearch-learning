-- 注意 MySQL 与 H2 的语法差异
-- H2: drop table tbl if exists;
-- MySQL: drop table if exists tbl;

DROP TABLE if EXISTS pms_product;

-- 商品表
CREATE TABLE `pms_product`
(
    `id`                    bigint      NOT NULL AUTO_INCREMENT,
    `brand_id`              bigint         DEFAULT NULL,
    `product_category_id`   bigint         DEFAULT NULL,
    `name`                  varchar(64) NOT NULL,
    `product_sn`            varchar(64) NOT NULL COMMENT '货号',
    `delete_status`         int            DEFAULT NULL COMMENT '删除状态：0->未删除；1->已删除',
    `publish_status`        int            DEFAULT NULL COMMENT '上架状态：0->下架；1->上架',
    `new_status`            int            DEFAULT NULL COMMENT '新品状态:0->不是新品；1->新品',
    `recommand_status`      int            DEFAULT NULL COMMENT '推荐状态；0->不推荐；1->推荐',
    `verify_status`         int            DEFAULT NULL COMMENT '审核状态：0->未审核；1->审核通过',
    `sort`                  int            DEFAULT NULL COMMENT '排序',
    `sale`                  int            DEFAULT NULL COMMENT '销量',
    `price`                 decimal(10, 2) DEFAULT NULL,
    `promotion_price`       decimal(10, 2) DEFAULT NULL COMMENT '促销价格',
    `gift_growth`           int            DEFAULT '0' COMMENT '赠送的成长值',
    `gift_point`            int            DEFAULT '0' COMMENT '赠送的积分',
    `use_point_limit`       int            DEFAULT NULL COMMENT '限制使用的积分数',
    `sub_title`             varchar(255)   DEFAULT NULL COMMENT '副标题',
    `description`           text COMMENT '商品描述',
    `original_price`        decimal(10, 2) DEFAULT NULL COMMENT '市场价',
    `stock`                 int            DEFAULT NULL COMMENT '库存',
    `low_stock`             int            DEFAULT NULL COMMENT '库存预警值',
    `unit`                  varchar(16)    DEFAULT NULL COMMENT '单位',
    `weight`                decimal(10, 2) DEFAULT NULL COMMENT '商品重量，默认为克',
    `preview_status`        int            DEFAULT NULL COMMENT '是否为预告商品：0->不是；1->是',
    `service_ids`           varchar(64)    DEFAULT NULL COMMENT '以逗号分割的产品服务：1->无忧退货；2->快速退款；3->免费包邮',
    `keywords`              varchar(255)   DEFAULT NULL,
    `note`                  varchar(255)   DEFAULT NULL,
    `detail_title`          varchar(255)   DEFAULT NULL,
    `detail_desc`           text,
    `promotion_start_time`  datetime       DEFAULT NULL COMMENT '促销开始时间',
    `promotion_end_time`    datetime       DEFAULT NULL COMMENT '促销结束时间',
    `promotion_per_limit`   int            DEFAULT NULL COMMENT '活动限购数量',
    `promotion_type`        int            DEFAULT NULL COMMENT '促销类型：0->没有促销使用原价;1->使用促销价；2->使用会员价；3->使用阶梯价格；4->使用满减价格；5->限时购',
    `brand_name`            varchar(255)   DEFAULT NULL COMMENT '品牌名称',
    `product_category_name` varchar(255)   DEFAULT NULL COMMENT '商品分类名称',
    PRIMARY KEY (`id`)
);

DROP TABLE if EXISTS pms_product_attribute;
-- 商品属性参数表
CREATE TABLE `pms_product_attribute`
(
    `id`                            bigint NOT NULL AUTO_INCREMENT,
    `product_attribute_category_id` bigint       DEFAULT NULL,
    `name`                          varchar(64)  DEFAULT NULL,
    `select_type`                   int          DEFAULT NULL COMMENT '属性选择类型：0->唯一；1->单选；2->多选',
    `input_type`                    int          DEFAULT NULL COMMENT '属性录入方式：0->手工录入；1->从列表中选取',
    `input_list`                    varchar(255) DEFAULT NULL COMMENT '可选值列表，以逗号隔开',
    `sort`                          int          DEFAULT NULL COMMENT '排序字段：最高的可以单独上传图片',
    `filter_type`                   int          DEFAULT NULL COMMENT '分类筛选样式：1->普通；1->颜色',
    `search_type`                   int          DEFAULT NULL COMMENT '检索类型；0->不需要进行检索；1->关键字检索；2->范围检索',
    `related_status`                int          DEFAULT NULL COMMENT '相同属性产品是否关联；0->不关联；1->关联',
    `hand_add_status`               int          DEFAULT NULL COMMENT '是否支持手动新增；0->不支持；1->支持',
    `type`                          int          DEFAULT NULL COMMENT '属性的类型；0->规格；1->参数',
    PRIMARY KEY (`id`)
);

DROP TABLE if EXISTS pms_brand;
-- 品牌表
CREATE TABLE `pms_brand`
(
    `id`                    bigint NOT NULL AUTO_INCREMENT,
    `name`                  varchar(64) DEFAULT NULL,
    `first_letter`          varchar(8)  DEFAULT NULL COMMENT '首字母',
    `sort`                  int         DEFAULT NULL,
    `factory_status`        int         DEFAULT NULL COMMENT '是否为品牌制造商：0->不是；1->是',
    `show_status`           int         DEFAULT NULL,
    `product_count`         int         DEFAULT NULL COMMENT '产品数量',
    `product_comment_count` int         DEFAULT NULL COMMENT '产品评论数量',
    `brand_story`           text COMMENT '品牌故事',
    PRIMARY KEY (`id`)
);

DROP TABLE if EXISTS pms_product_category;
-- 产品分类
CREATE TABLE `pms_product_category`
(
    `id`            bigint NOT NULL AUTO_INCREMENT,
    `parent_id`     bigint       DEFAULT NULL COMMENT '上机分类的编号：0表示一级分类',
    `name`          varchar(64)  DEFAULT NULL,
    `level`         int          DEFAULT NULL COMMENT '分类级别：0->1级；1->2级',
    `product_count` int          DEFAULT NULL,
    `product_unit`  varchar(64)  DEFAULT NULL,
    `nav_status`    int          DEFAULT NULL COMMENT '是否显示在导航栏：0->不显示；1->显示',
    `show_status`   int          DEFAULT NULL COMMENT '显示状态：0->不显示；1->显示',
    `sort`          int          DEFAULT NULL,
    `keywords`      varchar(255) DEFAULT NULL,
    `description`   text COMMENT '描述',
    PRIMARY KEY (`id`)
);

DROP TABLE if EXISTS pms_product_attribute_value;
-- 存储产品参数信息的表
CREATE TABLE `pms_product_attribute_value`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `product_id`           bigint      DEFAULT NULL,
    `product_attribute_id` bigint      DEFAULT NULL,
    `value`                varchar(64) DEFAULT NULL COMMENT '手动添加规格或参数的值，参数单值，规格有多个时以逗号隔开',
    PRIMARY KEY (`id`)
);
