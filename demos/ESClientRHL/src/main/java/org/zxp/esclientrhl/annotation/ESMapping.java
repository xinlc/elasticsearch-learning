package org.zxp.esclientrhl.annotation;


import org.zxp.esclientrhl.enums.Analyzer;
import org.zxp.esclientrhl.enums.DataType;

import java.lang.annotation.*;

/**
 * program: esdemo
 * description: 对应索引结构mapping的注解，在es entity field上添加
 * author: X-Pacific zhang
 * create: 2019-01-25 16:57
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface ESMapping {
    /**
     * 数据类型（包含 关键字类型）
     */
    DataType datatype() default DataType.text_type;
    /**
     * 间接关键字
     */
    boolean keyword() default true;
    /**
     * 关键字忽略字数
     */
    int ignore_above() default 256;
    /**
     * 是否支持ngram，高效全文搜索提示
     */
    boolean ngram() default false;
    /**
     * 是否支持suggest，高效前缀搜索提示
     */
    boolean suggest() default false;
    /**
     * 索引分词器设置（研究类型）
     */
    Analyzer analyzer() default Analyzer.standard;
    /**
     * 搜索内容分词器设置
     */
    Analyzer search_analyzer() default Analyzer.standard;
    //6+版本已经改变方式
//    /**
//     * 是否分析字段
//     * @return
//     */
//    AnalyzedType analyzedtype() default AnalyzedType.analyzed;

    /**
     * 是否允许被搜索
     */
    boolean allow_search() default true;

    /**
     * 拷贝到哪个字段，代替_all
     */
    String copy_to() default "";

    /**
     * null_value指定，默认空字符串不会为mapping添加null_value
     * 对于值是null的进行处理，当值为null是按照注解指定的‘null_value’值进行查询可以查到
     * 需要注意的是要与根本没有某字段区分（没有某字段需要用Exists Query进行查询）
     * 建议设置值为NULL_VALUE
     * @return
     */
    String null_value() default "";

    /**
     * nested对应的类型，默认为Object.Class。
     * 对于DataType是nested_type的类型才需要添加的注解，通过这个注解生成嵌套类型的索引
     * 例如：
     * @ESMapping(datatype = DataType.nested_type, nested_class = EsFundDto.class)
     *
     * @return
     */
    Class nested_class() default Object.class;
}
