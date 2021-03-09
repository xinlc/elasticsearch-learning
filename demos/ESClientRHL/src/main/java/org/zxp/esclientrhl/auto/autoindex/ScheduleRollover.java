package org.zxp.esclientrhl.auto.autoindex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.index.ElasticsearchIndex;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @program: esclientrhl
 * @description:
 * @author: X-Pacific zhang
 * @create: 2021-02-04 10:21
 **/
@Configuration
@Order(2)
public class ScheduleRollover implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {
    @Autowired
    ElasticsearchIndex elasticsearchIndex;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ApplicationContext applicationContext;

    /**
     * 扫描ESMetaData注解的类，并自动开启定时任务根据配置执行rollover
     * 特别注意，不支持分布式调度
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(event.getApplicationContext().getParent() != null){
            return;
        }
        Map<String, Object> beansWithAnnotationMap = this.applicationContext.getBeansWithAnnotation(ESMetaData.class);
        List<Map.Entry<String, Object>> autoRolloverBeanList = beansWithAnnotationMap.entrySet().stream().filter(e -> {
            boolean rollover = e.getValue().getClass().getAnnotation(ESMetaData.class).rollover();
            boolean autoRollover = e.getValue().getClass().getAnnotation(ESMetaData.class).autoRollover();
            if (rollover && autoRollover) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(autoRolloverBeanList.size());
        autoRolloverBeanList.forEach(s -> {
            ESMetaData annotation = s.getValue().getClass().getAnnotation(ESMetaData.class);
            executor.scheduleAtFixedRate(() -> {
                try {
                    logger.info("索引名称{} 执行rollover",annotation.indexName());
                    elasticsearchIndex.rollover(s.getValue().getClass(), false);
                } catch (Exception e) {
                    logger.error("ScheduleRollover scheduleAtFixedRate error", e);
                }
            }, annotation.autoRolloverInitialDelay(),annotation.autoRolloverPeriod(), annotation.autoRolloverTimeUnit());
        });
        logger.info("扫描到@ESMetaData注解bean，并需要自动执行rollover bean个数：{}",autoRolloverBeanList.size());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
