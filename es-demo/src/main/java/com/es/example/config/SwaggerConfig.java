package com.es.example.config;

import com.es.example.common.config.BaseSwaggerConfig;
import com.es.example.common.domain.SwaggerProperties;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * Swagger API文档相关配置
 */
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig extends BaseSwaggerConfig {

	@Override
	public SwaggerProperties swaggerProperties() {
		return SwaggerProperties.builder()
				.groupName("product")
				.apiBasePackage("com.es.example.controller")
				.title("搜索系统")
				.description("搜索相关接口文档")
				.contactName("Richard")
				.version("1.0")
				.enableSecurity(false)
				.build();
	}
}
