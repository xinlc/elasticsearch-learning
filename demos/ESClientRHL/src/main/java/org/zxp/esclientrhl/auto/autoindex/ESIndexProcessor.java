package org.zxp.esclientrhl.auto.autoindex;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.type.AnnotationMetadata;
import org.zxp.esclientrhl.annotation.EnableESTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.zxp.esclientrhl.auto.util.GetBasePackage;

/**
 * program: esdemo
 * description: spring初始化完成后通过读取启动类EnableESTools注解上entity的路径（或者不配置，取启动类所在包），得到路径后委托ESEntityScanner扫描相关路径
 * ESCRegistrar进行调用
 * author: X-Pacific zhang
 * create: 2019-01-30 17:22
 **/
//@Configuration
public class ESIndexProcessor {//implements BeanFactoryPostProcessor, ApplicationContextAware, BeanFactoryAware {
//    private static Logger logger = LoggerFactory.getLogger(ESIndexProcessor.class);
//
//    private ApplicationContext applicationContext;
//    private BeanFactory beanFactory;


    /**
     * 扫描ESMetaData注解的类entitypath或根路径的entity托管给spring
     * @param beanFactory
     * @throws BeansException
     */
    public void scan(AnnotationMetadata annotationMetadata,BeanFactory beanFactory,ApplicationContext applicationContext){
        GetBasePackage getBasePackage = new GetBasePackage(EnableESTools.class);
        ESEntityScanner scanner = new ESEntityScanner((BeanDefinitionRegistry) beanFactory);
        scanner.setResourceLoader(applicationContext);
        scanner.scan(getBasePackage.getEntityPackage(annotationMetadata).toArray(String[]::new));
    }


    /**
     * 扫描ESMetaData注解的类托管给spring
     * @param beanFactory
     * @throws BeansException
     */
//    @Override
    @Deprecated
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        Map<String,Object> beans = applicationContext.getBeansWithAnnotation(EnableESTools.class);
//        List<String> basePackages = AutoConfigurationPackages.get(this.beanFactory);
//        //根据@EnableESTools的配置获取要扫描的包，最终获得配置了ESMetaData注解的Entity
//        beans.forEach((beanName,bean) ->
//                {
//                    Class<?> anClass = bean.getClass();
//                    //获取启动注解信息
//                    String[] eps = anClass.getAnnotation(EnableESTools.class).entityPath();
//
//                    List<String> pathList = new ArrayList<>();
//                    for (int i = 0; i < eps.length; i++) {
//                        if(!StringUtils.isEmpty(eps[i])){
//                            pathList.add(eps[i]);
//                        }
//                    }
//                    if(pathList.size() == 0){
//                        eps = new String[1];
//                        pathList.addAll(basePackages);
//                    }
//                    eps = pathList.toArray(new String[pathList.size()]);
//                    //扫描entity
//                    ESEntityScanner scanner = new ESEntityScanner((BeanDefinitionRegistry) beanFactory);
//                    scanner.setResourceLoader(this.applicationContext);
//                    scanner.scan(eps);
//                }
//        );
//        //springbootstarter的方式，通过basePackages获取要扫描的包，最终获得配置了ESMetaData注解的Entity
//        if(beans == null || beans.isEmpty()){
//            List<String> pathList = new ArrayList<>();
//            pathList.addAll(basePackages);
//            String[] eps = pathList.toArray(new String[pathList.size()]);
//            //扫描entity
//            ESEntityScanner scanner = new ESEntityScanner((BeanDefinitionRegistry) beanFactory);
//            scanner.setResourceLoader(this.applicationContext);
//            scanner.scan(eps);
//        }
    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//
//    @Override
//    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
//        this.beanFactory = beanFactory;
//    }
}
