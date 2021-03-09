package org.zxp.esclientrhl.auto.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.zxp.esclientrhl.auto.intfproxy.ESCRepository;
import org.zxp.esclientrhl.auto.intfproxy.RepositoryFactorySupport;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * program: esclientrhl
 * description:
 * author: X-Pacific zhang
 * create: 2019-09-04 13:43
 **/
public abstract class AbstractESCRegister {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void registerBeanDefinitions(BeanFactory factory, Environment environment, ResourceLoader resourceLoader, AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        getCandidates(annotationMetadata, registry, environment, resourceLoader).forEach(beanDefinition -> {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RepositoryFactorySupport.class);
            String beanClassName = beanDefinition.getBeanClassName();
            //传入要实例化的接口
            beanDefinitionBuilder.addConstructorArgValue(beanClassName);
            //获取bean的定义
            BeanDefinition bd = beanDefinitionBuilder.getRawBeanDefinition();
            //生成beanname
            String beanName = beanClassName.substring(beanClassName.lastIndexOf(".") + 1);
            if(org.zxp.esclientrhl.auto.util.EnableESTools.isPrintregmsg()){
                logger.info("generate ESCRegistrar beanClassName:" + beanClassName);
                logger.info("generate ESCRegistrar beanName:" + beanName);
            }
            BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) factory;
            //注册bean  beanName是代理bean的名字
            beanDefinitionRegistry.registerBeanDefinition(beanName, bd);
        });
    }

    /**
     * 扫描ESCRepository接口的类型并作为候选人返回
     *
     * @param registry
     * @return
     */
    public Stream<BeanDefinition> getCandidates(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry, Environment environment, ResourceLoader resourceLoader) {
        ESCRepositoryComponentProvider scanner = new ESCRepositoryComponentProvider(registry);
        scanner.setEnvironment(environment);
        scanner.setResourceLoader(resourceLoader);
        //输入是basepackages，输出是BeanDefinition的Stream
        return getBasePackage(annotationMetadata).flatMap(it -> scanner.findCandidateComponents(it).stream());
    }

    /**
     * 必须子类实现，autoconfig方式不同
     * @param annotationMetadata
     * @return
     */
    public abstract Stream<String> getBasePackage(AnnotationMetadata annotationMetadata);

    /**
     * scanner interface ESCRepository
     */
    private static class ESCRepositoryComponentProvider extends ClassPathScanningCandidateComponentProvider {
        private BeanDefinitionRegistry registry;

        public ESCRepositoryComponentProvider(BeanDefinitionRegistry registry) {
            super(false);
            Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");
            super.addIncludeFilter(new InterfaceTypeFilter(ESCRepository.class));
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            boolean isNonRepositoryInterface = !isGenericRepositoryInterface(beanDefinition.getBeanClassName());
            boolean isTopLevelType = !beanDefinition.getMetadata().hasEnclosingClass();
            boolean isConsiderNestedRepositories = false;
            return isNonRepositoryInterface && (isTopLevelType || isConsiderNestedRepositories);
        }

        private static boolean isGenericRepositoryInterface(@Nullable String interfaceName) {
            return ESCRepository.class.getName().equals(interfaceName);
        }
    }

    private static class InterfaceTypeFilter extends AssignableTypeFilter {
        public InterfaceTypeFilter(Class<?> targetType) {
            super(targetType);
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                throws IOException {
            return metadataReader.getClassMetadata().isInterface() && super.match(metadataReader, metadataReaderFactory);
        }
    }
}
