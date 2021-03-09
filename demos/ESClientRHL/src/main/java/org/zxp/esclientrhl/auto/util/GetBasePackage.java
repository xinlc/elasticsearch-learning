package org.zxp.esclientrhl.auto.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

/**
 * program: esclientrhl
 * description: 获取basepackage列表
 * author: X-Pacific zhang
 * create: 2019-09-02 22:12
 **/
public class GetBasePackage {
    //缓存的entitypaths
    private static Map<Class,List<String>> entityPathsMap = null;

    static {
        entityPathsMap = new HashMap<>();
    }


    private Class<? extends Annotation> annotation;

    public GetBasePackage(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * 获取repository的路径，如果获取不到就取main启动路径
     * @param annotationMetadata
     * @return
     */
    public Stream<String> getBasePackage(AnnotationMetadata annotationMetadata){
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(annotation.getName());
        AnnotationAttributes attributes = new AnnotationAttributes(annotationAttributes);
        EnableESTools.gainAnnoInfo(attributes);
        String[] value = EnableESTools.getValue();//annotationg中的注解
        String[] basePackages = EnableESTools.getBasePackages();//annotationg中的注解
        String[] entityPaths = EnableESTools.getEntityPath();//annotationg中的注解
        //没配注解参数
        if (value.length == 0 && basePackages.length == 0) {
            String className = annotationMetadata.getClassName();
            return  Stream.of(ClassUtils.getPackageName(className));
        }
        //配了注解
        return Stream.of(Arrays.asList(value),Arrays.asList(basePackages),Arrays.asList(entityPaths)).flatMap(list -> list.stream());
    }

    /**
     * 获取实体类的路径，如果获取不到就取main启动路径
     * @param annotationMetadata
     * @return
     */
    public Stream<String> getEntityPackage(AnnotationMetadata annotationMetadata){
        //缓存entitypaths
        if(entityPathsMap.containsKey(annotation)){
            return Stream.of(entityPathsMap.get(annotation)).flatMap(list -> list.stream());
        }
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(annotation.getName());
        AnnotationAttributes attributes = new AnnotationAttributes(annotationAttributes);
        String[] entityPaths = attributes.getStringArray("entityPath");//annotationg中的注解
        //没配注解参数
        if (entityPaths.length == 0) {
            String className = annotationMetadata.getClassName();
            entityPathsMap.put(annotation,Arrays.asList(ClassUtils.getPackageName(className)));
            return  Stream.of(ClassUtils.getPackageName(className));
        }
        entityPathsMap.put(annotation,Arrays.asList(entityPaths));
        //配了注解
        return Stream.of(Arrays.asList(entityPaths)).flatMap(list -> list.stream());
    }

    public static Map<Class, List<String>> getEntityPathsMap() {
        return entityPathsMap;
    }
}
