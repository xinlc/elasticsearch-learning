package org.zxp.esclientrhl.annotation;

import org.zxp.esclientrhl.auto.ESCRegistrar;
import org.zxp.esclientrhl.config.ElasticSearchConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * program: esdemo
 * description: springboot启动类配置该注解，该注解有两个作用
 * 作用1：引入自动配置的restHighLevelClient
 * 作用2：配置entityPath以识别es entity自动创建索引以及mapping（如果不配置，则按照Main方法的路径包进行扫描）
 * 作用3：引入ESToolsRegistrar准备实现切面实现接口的功能（参考JPA实现）
 * author: X-Pacific zhang
 * create: 2019-01-24 11:45
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ElasticSearchConfiguration.class,ESCRegistrar.class})
public @interface EnableESTools {

    /**
     * 配置repository包路径,如果不配置，则按照Main方法的路径包进行扫描
     * @return
     */
    String[] basePackages() default {};
    /**
     * 配置repository包路径,如果不配置，则按照Main方法的路径包进行扫描
     * @return
             */
    String[] value() default {};
    /**
     * entity路径配置,如果不配置，则按照Main方法的路径包进行扫描
     */
    String[] entityPath() default {};

    /**
     * 是否打印注册信息
     * @return
     */
    boolean printregmsg() default false;
}
