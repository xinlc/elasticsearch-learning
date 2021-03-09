package com.es.example.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis相关配置
 */
@Configuration
@MapperScan({"com.es.example.mapper", "com.es.example.dao"})
public class MyBatisConfig {
}
