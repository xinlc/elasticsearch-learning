package org.zxp.esclientrhl.auto.intfproxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.zxp.esclientrhl.annotation.EnableESTools;
import org.zxp.esclientrhl.auto.util.GetBasePackage;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * program: esclientrhl
 * description: 用于生成ESCRepository的代理bean
 * author: X-Pacific zhang
 * create: 2019-09-02 23:09
 **/
public class RepositoryFactorySupport<T extends ESCRepository<S, ID>, S, ID> implements ApplicationContextAware, ResourceLoaderAware, InitializingBean, FactoryBean<T>, BeanClassLoaderAware,
        BeanFactoryAware, ApplicationEventPublisherAware {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Class<? extends T> repositoryInterface;
    private ResourceLoader resourceLoader;
    private T repository;
    private ClassLoader classLoader;
    private BeanFactory beanFactory;
    private ApplicationEventPublisher publisher;
    private ApplicationContext applicationContext;

    public RepositoryFactorySupport(Class<? extends T> repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            this.repository = this.getRepository(repositoryInterface);
        } catch (Exception e) {
            logger.error("ESCRepository proxy create fail !", e);
        }
    }


    public <T> T getRepository(Class<T> repositoryInterface) throws Exception {
        SimpleESCRepository target = new SimpleESCRepository(applicationContext);
        getMetadata(target);
        ProxyFactory result = new ProxyFactory();
        result.setTarget(target);
        result.addAdvice(new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
                Object result = invocation.proceed();
                return result;
            }
        });
        result.setInterfaces(this.repositoryInterface, ESCRepository.class);
        T repository = (T) result.getProxy(classLoader);
        return repository;
    }

    /**
     * 根据interface获取实体类类型以及主键类型
     *
     * @param target
     */
    private void getMetadata(SimpleESCRepository target) throws Exception {
        Type[] types = repositoryInterface.getGenericInterfaces();
        ParameterizedType parameterized = (ParameterizedType) types[0];
        //实体类类型名称
        String domainClassName = parameterized.getActualTypeArguments()[0].getTypeName();
        //实体类主键类型名称
        String idClassName = parameterized.getActualTypeArguments()[1].getTypeName();
        if (org.zxp.esclientrhl.auto.util.EnableESTools.isPrintregmsg()) {
            logger.info("domainClassName：" + domainClassName + " idClassName：" + idClassName);
        }
        //按照实体类类型名称匹配实体类类型
        List<String> entityList = getEntityList();
        for (int i = 0; i < entityList.size(); i++) {
            if (entityList.get(i).lastIndexOf("." + domainClassName) != -1 || entityList.get(i).equals(domainClassName)) {
                if (target.getDomainClass() == null) {
                    target.setDomainClass(Class.forName(entityList.get(i)));
                    break;
                } else {
                    target.setDomainClass(null);
                    throw new Exception("Entity Overmatched !");
                }
            }
        }
        //按照实体类主键类型名称主键类型
        Map<String, Class> idTypeMap = getIdTypeMap();
        if (idTypeMap.containsKey(idClassName)) {
            target.setIdClass(idTypeMap.get(idClassName));
        } else {
            throw new Exception("Not Supported ID Type !");
        }
    }

    private Map<String, Class> getIdTypeMap() {
        Map<String, Class> idTypeMap = new HashMap<>();
        idTypeMap.put("String", String.class);
        idTypeMap.put("Integer", Integer.class);
        idTypeMap.put("Long", Long.class);
        idTypeMap.put("java.lang.String", String.class);
        idTypeMap.put("java.lang.Integer", Integer.class);
        idTypeMap.put("java.lang.Long", Long.class);
        return idTypeMap;
    }


    /**
     * 获取实体类路径上的所有全限定类命
     *
     * @return
     */
    private List<String> getEntityList() {
        List<String> entityList = new ArrayList<>();
        GetBasePackage.getEntityPathsMap().get(EnableESTools.class).forEach(s -> {
            ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
            MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
            Resource[] resources = new Resource[0];
            try {
                resources = resolver.getResources("classpath*:" + s.replaceAll("\\.", "/") + "/**/*.class");
                for (Resource r : resources) {
                    MetadataReader reader = metaReader.getMetadataReader(r);
                    entityList.add(reader.getClassMetadata().getClassName());
                }
            } catch (IOException e) {
                logger.error("getEntityList error",e);
            }
        });
        return entityList;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 实现了FactoryBean可以将生成的代理bean托管给spring
     *
     * @return
     * @throws Exception
     */
    @Override
    public T getObject() throws Exception {
        return this.repository;
    }

    /**
     * 实现了FactoryBean可以将生成的代理bean托管给spring
     *
     * @return
     */
    @Override
    public Class<?> getObjectType() {
        return repositoryInterface;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
