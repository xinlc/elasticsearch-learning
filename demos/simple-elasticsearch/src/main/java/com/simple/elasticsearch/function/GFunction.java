package com.simple.elasticsearch.function;

import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author zcw
 * @version 1.0
 * @date 2021/1/14 13:50
 */
@FunctionalInterface
public interface GFunction<T, R> extends Function<T, R>, Serializable {

    String get = "get";

    String is = "is";

    @SneakyThrows
    private SerializedLambda getSerializedLambda() {
        Method write = this.getClass().getDeclaredMethod("writeReplace");
        write.setAccessible(true);
        return (SerializedLambda) write.invoke(this);
    }

    default String field() {
        SerializedLambda serializedLambda = this.getSerializedLambda();
        String methodName = serializedLambda.getImplMethodName();
        return resolveFieldName(methodName);
    }

    private String resolveFieldName(String getMethodName) {
        if (getMethodName.startsWith(get)) {
            getMethodName = getMethodName.substring(3);
        } else if (getMethodName.startsWith(is)) {
            getMethodName = getMethodName.substring(2);
        }
        getMethodName = getMethodName.substring(0, 1).toLowerCase() + getMethodName.substring(1);
        return getMethodName;
    }

}
