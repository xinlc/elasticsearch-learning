package org.zxp.esclientrhl.util;

import org.zxp.esclientrhl.annotation.ESID;
import org.springframework.util.StringUtils;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.enums.DataType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * program: esdemo
 * description: 工具类
 * author: X-Pacific zhang
 * create: 2019-01-18 16:23
 **/
public class Tools {
    /**
     * 根据对象中的注解获取ID的字段值
     * @param obj
     * @return
     */
    public static String getESId(Object obj) throws Exception {
        Field[] fields = obj.getClass().getDeclaredFields();
        for(Field f : fields){
            f.setAccessible(true);
            ESID esid = f.getAnnotation(ESID.class);
            if(esid != null){
                Object value = f.get(obj);
                if(value == null){
                    return null;
                }else{
                    return value.toString();
                }
            }
        }
        return null;
    }

    /**
     * 获取o中所有的字段有值的map组合
     * @return
     */
    public static Map getFieldValue(Object o) throws IllegalAccessException {
        Map retMap = new HashMap();
        Field[] fs = o.getClass().getDeclaredFields();
        for(int i = 0;i < fs.length;i++){
            Field f = fs[i];
            f.setAccessible(true);
            if(f.get(o) != null){
                retMap.put(f.getName(),f.get(o) );
            }
        }
        return retMap;
    }

    /**
     * 通过反射,获得定义Class时声明的父类的范型参数的类型.
     *
     * @param clazz The class to introspect
     * @return the first generic declaration, or <code>Object.class</code> if cannot be determined
     */
    public static Class getSuperClassGenricType(Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * 通过反射,获得定义Class时声明的父类的范型参数的类型. 如public BookManager extends GenricManager<Book>
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     */
    public static Class getSuperClassGenricType(Class clazz, int index)
            throws IndexOutOfBoundsException {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }

    public static String arraytostring(String[] strs){
        if(StringUtils.isEmpty(strs)){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        Arrays.asList(strs).stream().forEach(str -> sb.append(str).append(" "));
        return sb.toString();
    }

    public static boolean arrayISNULL(Object[] objs){
        if(objs == null || objs.length == 0){
            return true;
        }
        boolean flag = false;
        for (int i = 0; i < objs.length; i++) {
            if(!StringUtils.isEmpty(objs[i])){
                flag = true;
            }
        }
        if(flag){
            return false;
        }else{
            return true;
        }
    }

    public static <T> List<List<T>> splitList(List<T> oriList,boolean isParallel){
        if(oriList.size() <=  Constant.BULK_COUNT){
            List<List<T>> splitList = new ArrayList<>();
            splitList.add(oriList);
            return splitList;
        }
        int limit = (oriList.size() + Constant.BULK_COUNT - 1) / Constant.BULK_COUNT;
        if(isParallel){
            return Stream.iterate(0, n -> n + 1).limit(limit).parallel().map(a -> oriList.stream().skip(a * Constant.BULK_COUNT).limit(Constant.BULK_COUNT).parallel().collect(Collectors.toList())).collect(Collectors.toList());
        }else{
            final List<List<T>> splitList = new ArrayList<>();
            Stream.iterate(0, n -> n + 1).limit(limit).forEach(i -> {
                splitList.add(oriList.stream().skip(i * Constant.BULK_COUNT ).limit(Constant.BULK_COUNT ).collect(Collectors.toList()));
            });
            return splitList;
        }
    }

    /**
     * 判断当前类是否包含nested字段
     */
    private static Map<Class,Boolean> checkNested = new HashMap<>();

    public static boolean checkNested(List list){
        if(list == null || list.size() == 0){
            return false;
        }
        return checkNested(list.get(0));
    }
    public static boolean checkNested(Object obj){
        if(obj == null){
            return false;
        }
        if(checkNested.containsKey(obj.getClass())){
            return checkNested.get(obj.getClass());
        }else {
            for (int i = 0; i < obj.getClass().getDeclaredFields().length; i++) {
                Field f = obj.getClass().getDeclaredFields()[i];
                if (f.getAnnotation(ESMapping.class)!= null
                        && (f.getAnnotation(ESMapping.class).datatype() == DataType.nested_type
                             || (f.getAnnotation(ESMapping.class).nested_class() != null && f.getAnnotation(ESMapping.class).nested_class() != Object.class))) {
                    checkNested.put(obj.getClass(), true);
                    return true;
                }
            }
            checkNested.put(obj.getClass(), false);
            return false;
        }
    }
}
