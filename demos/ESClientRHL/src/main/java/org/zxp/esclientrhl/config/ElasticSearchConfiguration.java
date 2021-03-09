package org.zxp.esclientrhl.config;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.zxp.esclientrhl.util.Constant;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;

/**
 * program: esdemo
 * description: 自动配置注入restHighLevelClient
 * author: X-Pacific zhang
 * create: 2019-01-07 14:09
 **/
@Configuration
@ComponentScan("org.zxp.esclientrhl")
public class ElasticSearchConfiguration  {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

//    @Value("${elasticsearch.host}")
//    private String host;
    @Autowired
    ElasticsearchProperties elasticsearchProperties;

    private RestHighLevelClient restHighLevelClient;

//    由于@Bean(destroyMethod="close")，所以不需要下面注释掉的释放方式
//    public void close() {
//        if (restHighLevelClient != null) {
//            try {
//                restHighLevelClient.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @PreDestroy
//    public void destroyMethod() throws Exception {
//        close();
//    }

    @Bean(destroyMethod="close")//这个close是调用RestHighLevelClient中的close
    @Scope("singleton")
    public RestHighLevelClient createInstance() {
        String host = elasticsearchProperties.getHost();
        String username = elasticsearchProperties.getUsername();
        String password = elasticsearchProperties.getPassword();
        Integer maxConnectTotal = elasticsearchProperties.getMaxConnectTotal();
        Integer maxConnectPerRoute = elasticsearchProperties.getMaxConnectPerRoute();
        Integer connectionRequestTimeoutMillis = elasticsearchProperties.getConnectionRequestTimeoutMillis();
        Integer socketTimeoutMillis = elasticsearchProperties.getSocketTimeoutMillis();
        Integer connectTimeoutMillis = elasticsearchProperties.getConnectTimeoutMillis();
        try {
            if(StringUtils.isEmpty(host)){
                host = Constant.DEFAULT_ES_HOST;
            }
            String[] hosts = host.split(",");
            HttpHost[] httpHosts = new HttpHost[hosts.length];
            for (int i = 0; i < httpHosts.length; i++) {
                String h = hosts[i];
                httpHosts[i] = new HttpHost(h.split(":")[0], Integer.parseInt(h.split(":")[1]), "http");
            }

            RestClientBuilder builder = RestClient.builder(httpHosts);
            builder.setRequestConfigCallback(requestConfigBuilder -> {
                requestConfigBuilder.setConnectTimeout(connectTimeoutMillis);
                requestConfigBuilder.setSocketTimeout(socketTimeoutMillis);
                requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeoutMillis);
                return requestConfigBuilder;
            });

            if(!StringUtils.isEmpty(username)) {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(username, password));  //es账号密码（默认用户名为elastic）

                builder.setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.disableAuthCaching();
                    httpClientBuilder.setMaxConnTotal(maxConnectTotal);
                    httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpClientBuilder;
                });
            }else{
                builder.setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.disableAuthCaching();
                    httpClientBuilder.setMaxConnTotal(maxConnectTotal);
                    httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                    return httpClientBuilder;
                });
            }

            restHighLevelClient = new RestHighLevelClient(builder);
        } catch (Exception e) {
            logger.error("create RestHighLevelClient error", e);
            return null;
        }
        return restHighLevelClient;
    }
}
