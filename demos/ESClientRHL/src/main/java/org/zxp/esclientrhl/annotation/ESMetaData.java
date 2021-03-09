package org.zxp.esclientrhl.annotation;

import org.elasticsearch.common.unit.ByteSizeUnit;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * program: esdemo
 * description: es索引元数据的注解，在es entity class上添加
 * author: X-Pacific zhang
 * create: 2019-01-18 16:12
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ESMetaData {
    /**
     * 检索时的索引名称，如果不配置则默认为和indexName一致，该注解项仅支持搜索
     * 并不建议这么做，建议通过特定方法来做跨索引查询
     */
    String[] searchIndexNames() default {};
    /**
     * 索引名称，必须配置
     */
    String indexName();
    /**
     * 索引类型，可以不配置，不配置默认和indexName相同，墙裂建议每个index下只有一个type
     */
    String indexType() default "";
    /**
     * 主分片数量
     * @return
     */
    int number_of_shards() default 1;
    /**
     * 备份分片数量
     * @return
     */
    int number_of_replicas() default 1;
    /**
     * 是否打印日志
     * @return
     */
    boolean printLog() default false;

    /**
     * 别名、如果配置了后续增删改查都基于这个alias
     * 当配置了此项后自动创建索引功能将失效
     * indexName为aliasName
     * @return
     */
    boolean alias() default false;

    /**
     * 别名对应的索引名称
     * 当前配置仅生效于配置了alias但没有配置rollover
     * 注意：所有配置的index必须存在
     * @return
     */
    String[] aliasIndex() default {};

    /**
     * 当配置了alias后，指定哪个index为writeIndex
     * 当前配置仅生效于配置了alias但没有配置rollover
     * 注意：配置的index必须存在切在aliasIndex中
     * @return
     */
    String writeIndex() default "";

    /**
     * 当配置了rollover为true时，开启rollover功能（并忽略其他alias的配置）
     * aliasName为indexName
     * 索引名字规格为：indexName-yyyy.mm.dd-00000n
     * 索引滚动生成策略如下
     * @return
     */
    boolean rollover() default false;


    /**
     * 自动执行rollover相关配置
     * 自动执行rollover开关
     * @return
     */
    boolean autoRollover() default false;

    /**
     * 自动执行rollover相关配置
     * 项目启动后延迟autoRolloverInitialDelay时间后开始执行
     * @return
     */
    long autoRolloverInitialDelay() default 0L;

    /**
     * 自动执行rollover相关配置
     * 项目启动后每间隔autoRolloverPeriod执行一次
     * @return
     */
    long autoRolloverPeriod() default 4L;

    /**
     * 自动执行rollover相关配置
     * 单位时间配置，与autoRolloverPeriod、autoRolloverInitialDelay对应
     * @return
     */
    TimeUnit  autoRolloverTimeUnit() default TimeUnit.HOURS;

    /**
     * 当前索引超过此项配置的时间后生成新的索引
     * @return
     */
    long rolloverMaxIndexAgeCondition() default 0L;

    /**
     * 与rolloverMaxIndexAgeCondition联合使用，对应rolloverMaxIndexAgeCondition的单位
     * @return
     */
    TimeUnit rolloverMaxIndexAgeTimeUnit() default TimeUnit.DAYS;

    /**
     * 当前索引文档数量超过此项配置的数字后生成新的索引
     * @return
     */
    long rolloverMaxIndexDocsCondition() default 0L;

    /**
     * 当前索引大小超过此项配置的数字后生成新的索引
     * @return
     */
    long rolloverMaxIndexSizeCondition() default 0L;

    /**
     * 与rolloverMaxIndexSizeCondition联合使用，对应rolloverMaxIndexSizeCondition的单位
     * @return
     */
    ByteSizeUnit rolloverMaxIndexSizeByteSizeUnit() default ByteSizeUnit.GB;


    /**
     * 最大分页深度
     * @return
     */
    long maxResultWindow() default 10000L;

    /**
     * 索引名称是否自动包含后缀
     * @return
     */
    boolean suffix() default false;

    /**
     * 是否自动创建索引
     * @return
     */
    boolean autoCreateIndex() default true;

}
