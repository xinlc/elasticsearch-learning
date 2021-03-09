package com.simple.elasticsearch.annotation;

import java.lang.annotation.*;

/**
 * @author zcw
 * @version 1.0
 * @date 2021/1/14 11:54
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EsId {
}
