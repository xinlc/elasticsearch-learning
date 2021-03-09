package org.zxp.esclientrhl.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.config.ElasticsearchProperties;
import org.zxp.esclientrhl.enums.DataType;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

/**
 * program: esdemo
 * description: 索引信息操作工具类
 * author: X-Pacific zhang
 * create: 2019-01-29 14:29
 **/
@Component
public class IndexTools {
    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    ///**
    // * 获取索引元数据：indexname、indextype
    // * @param clazz
    // * @return
    // */
    //public static MetaData getIndexType(Class<?> clazz){
    //    String indexname = "";
    //    String indextype = "";
    //    if(clazz.getAnnotation(ESMetaData.class) != null){
    //        indexname = clazz.getAnnotation(ESMetaData.class).indexName();
    //        indextype = clazz.getAnnotation(ESMetaData.class).indexType();
    //        if(indextype == null || indextype.equals("")){indextype = "_doc";}
    //        MetaData metaData = new MetaData(indexname,indextype);
    //        metaData.setPrintLog(clazz.getAnnotation(ESMetaData.class).printLog());
    //        if(Tools.arrayISNULL(clazz.getAnnotation(ESMetaData.class).searchIndexNames())) {
    //            metaData.setSearchIndexNames(new String[]{indexname});
    //        }else{
    //            metaData.setSearchIndexNames((clazz.getAnnotation(ESMetaData.class).searchIndexNames()));
    //        }
    //        return metaData;
    //    }
    //    return null;
    //}
    //
    ///**
    // * 获取索引元数据：主分片、备份分片数的配置
    // * @param clazz
    // * @return
    // */
    //public static MetaData getShardsConfig(Class<?> clazz){
    //    int number_of_shards = 0;
    //    int number_of_replicas = 0;
    //    if(clazz.getAnnotation(ESMetaData.class) != null){
    //        number_of_shards = clazz.getAnnotation(ESMetaData.class).number_of_shards();
    //        number_of_replicas = clazz.getAnnotation(ESMetaData.class).number_of_replicas();
    //        MetaData metaData = new MetaData(number_of_shards,number_of_replicas);
    //        metaData.setPrintLog(clazz.getAnnotation(ESMetaData.class).printLog());
    //        return metaData;
    //    }
    //    return null;
    //}

    /**
     * 获取索引元数据：indexname、indextype、主分片、备份分片数的配置
     * @param clazz
     * @return
     */
    public MetaData getMetaData(Class<?> clazz){
        MetaData metaData = null;
        if(clazz.getAnnotation(ESMetaData.class) != null){
            String indexname = "";
            String indextype = "";
            int number_of_shards = 0;
            int number_of_replicas = 0;
            indexname = clazz.getAnnotation(ESMetaData.class).indexName();
            indextype = clazz.getAnnotation(ESMetaData.class).indexType();
            //es7 https://www.elastic.co/guide/en/elasticsearch/reference/7.9/removal-of-types.html
            if(indextype == null || indextype.equals("")){indextype =  "_doc";}
            number_of_shards = clazz.getAnnotation(ESMetaData.class).number_of_shards();
            number_of_replicas = clazz.getAnnotation(ESMetaData.class).number_of_replicas();
            metaData = new MetaData(indexname,indextype,number_of_shards,number_of_replicas);
            //如果配置了Suffix则自动添加后缀到索引名称
            if(clazz.getAnnotation(ESMetaData.class).suffix()) {
                metaData.setSuffix(elasticsearchProperties.getSuffix());
                if(metaData.getSuffix() != null && !"".equals(metaData.getSuffix())){
                    metaData.setIndexname(metaData.getIndexname()+"_"+metaData.getSuffix());
                    indexname = metaData.getIndexname();
                }
            }
            metaData.setPrintLog(clazz.getAnnotation(ESMetaData.class).printLog());
            if(Tools.arrayISNULL(clazz.getAnnotation(ESMetaData.class).searchIndexNames())) {
                metaData.setSearchIndexNames(new String[]{indexname});
            }else{
                //如果配置了searchIndexNames，则以配置为准
                metaData.setSearchIndexNames((clazz.getAnnotation(ESMetaData.class).searchIndexNames()));
            }
            metaData.setAlias(clazz.getAnnotation(ESMetaData.class).alias());
            metaData.setAliasIndex(clazz.getAnnotation(ESMetaData.class).aliasIndex());
            metaData.setWriteIndex(clazz.getAnnotation(ESMetaData.class).writeIndex());
            metaData.setRollover(clazz.getAnnotation(ESMetaData.class).rollover());
            metaData.setRolloverMaxIndexAgeCondition(clazz.getAnnotation(ESMetaData.class).rolloverMaxIndexAgeCondition());
            metaData.setRolloverMaxIndexAgeTimeUnit(clazz.getAnnotation(ESMetaData.class).rolloverMaxIndexAgeTimeUnit());
            metaData.setRolloverMaxIndexDocsCondition(clazz.getAnnotation(ESMetaData.class).rolloverMaxIndexDocsCondition());
            metaData.setRolloverMaxIndexSizeCondition(clazz.getAnnotation(ESMetaData.class).rolloverMaxIndexSizeCondition());
            metaData.setRolloverMaxIndexSizeByteSizeUnit(clazz.getAnnotation(ESMetaData.class).rolloverMaxIndexSizeByteSizeUnit());
            metaData.setMaxResultWindow(clazz.getAnnotation(ESMetaData.class).maxResultWindow());
            metaData.setAutoRollover(clazz.getAnnotation(ESMetaData.class).autoRollover());
            metaData.setAutoCreateIndex(clazz.getAnnotation(ESMetaData.class).autoCreateIndex());
            return metaData;
        }else{
            throw new IllegalArgumentException("未配置@ESMetaData注解");
        }
    }

    /**
     * 获取配置于Field上的mapping信息，如果未配置注解，则给出默认信息
     * @param field
     * @return
     */
    public MappingData getMappingData(Field field){
        if(field == null){
            return null;
        }
        field.setAccessible(true);
        MappingData mappingData = new MappingData();
        mappingData.setField_name(field.getName());
        if(field.getAnnotation(ESMapping.class) != null){
            ESMapping esMapping = field.getAnnotation(ESMapping.class);
            mappingData.setDatatype(getType(esMapping.datatype()));
            mappingData.setAnalyzer(esMapping.analyzer().toString());
            mappingData.setNgram(esMapping.ngram());
            mappingData.setIgnore_above(esMapping.ignore_above());
            mappingData.setSearch_analyzer(esMapping.search_analyzer().toString());
            if(mappingData.getDatatype().equals("text")) {
                mappingData.setKeyword(esMapping.keyword());
            }else{
                mappingData.setKeyword(false);
            }
            mappingData.setSuggest(esMapping.suggest());
            mappingData.setAllow_search(esMapping.allow_search());
            mappingData.setCopy_to(esMapping.copy_to());
            mappingData.setNested_class(esMapping.nested_class());
            if(!StringUtils.isEmpty(esMapping.null_value())){
                mappingData.setNull_value(esMapping.null_value());
            }
        }else{
            mappingData.setKeyword(false);
            if(field.getAnnotation(ESID.class) != null){
                mappingData.setDatatype(getType(DataType.keyword_type));
            }else{
                if(field.getType() == String.class){
                    mappingData.setDatatype(getType(DataType.text_type));
                    mappingData.setKeyword(true);
                }
                else if(field.getType() == Short.class || field.getType() == short.class){
                    mappingData.setDatatype(getType(DataType.short_type));
                }
                else if(field.getType() == Integer.class || field.getType() == int.class){
                    mappingData.setDatatype(getType(DataType.integer_type));
                }
                else if(field.getType() == Long.class || field.getType() == long.class){
                    mappingData.setDatatype(getType(DataType.long_type));
                }
                else if(field.getType() == Float.class || field.getType() == float.class){
                    mappingData.setDatatype(getType(DataType.float_type));
                }
                else if(field.getType() == Double.class || field.getType() == double.class){
                    mappingData.setDatatype(getType(DataType.double_type));
                }
                else if(field.getType() == BigDecimal.class){
                    mappingData.setDatatype(getType(DataType.double_type));
                }
                else if(field.getType() == Boolean.class || field.getType() == boolean.class){
                    mappingData.setDatatype(getType(DataType.boolean_type));
                }
                else if(field.getType() == Byte.class || field.getType() == byte.class){
                    mappingData.setDatatype(getType(DataType.byte_type));
                }
                else if(field.getType() == Date.class){
                    mappingData.setDatatype(getType(DataType.date_type));
                }
                else{
                    mappingData.setDatatype(getType(DataType.text_type));
                    mappingData.setKeyword(true);
                }
            }
            mappingData.setAnalyzer("standard");
            mappingData.setNgram(false);
            mappingData.setIgnore_above(256);
            mappingData.setSearch_analyzer("standard");
            mappingData.setSuggest(false);
            mappingData.setAllow_search(true);
            mappingData.setCopy_to("");
            mappingData.setNested_class(null);
        }
        return mappingData;
    }

    /**
     * 根据枚举类型获得mapping中的类型
     * @param dataType
     * @return
     */
    private static String getType(DataType dataType){
        return dataType.toString().replaceAll("_type","");
    }

    /**
     * 批量获取配置于Field上的mapping信息，如果未配置注解，则给出默认信息
     * @param clazz
     * @return
     */
    public MappingData[] getMappingData(Class<?> clazz){
        Field[] fields = clazz.getDeclaredFields();
        MappingData[] mappingDataList = new MappingData[fields.length];
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].getName().equals("serialVersionUID")){
                continue;
            }
            mappingDataList[i] = getMappingData(fields[i]);
        }
        return mappingDataList;
    }
}
