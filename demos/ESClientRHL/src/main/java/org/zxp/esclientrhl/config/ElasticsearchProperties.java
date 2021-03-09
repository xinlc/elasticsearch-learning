package org.zxp.esclientrhl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @program: esclientrhl
 * @description:
 * @author: X-Pacific zhang
 * @create: 2019-10-23 15:52
 **/
@Component
public class ElasticsearchProperties {
    @Value("${elasticsearch.host:127.0.0.1:9200}")
    private String host;
    @Value("${elasticsearch.username:}")
    private String username;
    @Value("${elasticsearch.password:}")
    private String password;
    /**
     * 连接池里的最大连接数
     */
    @Value("${elasticsearch.max_connect_total:30}")
    private Integer maxConnectTotal;

    /**
     * 某一个/每服务每次能并行接收的请求数量
     */
    @Value("${elasticsearch.max_connect_per_route:10}")
    private Integer maxConnectPerRoute;

    /**
     * http clilent中从connetcion pool中获得一个connection的超时时间
     */
    @Value("${elasticsearch.connection_request_timeout_millis:2000}")
    private Integer connectionRequestTimeoutMillis;

    /**
     * 响应超时时间，超过此时间不再读取响应
     */
    @Value("${elasticsearch.socket_timeout_millis:30000}")
    private Integer socketTimeoutMillis;

    /**
     * 链接建立的超时时间
     */
    @Value("${elasticsearch.connect_timeout_millis:2000}")
    private Integer connectTimeoutMillis;

    /**
     * 索引后后缀配置
     */
    @Value("${elasticsearch.index.suffix:}")
    private String suffix;


    public Integer getMaxConnectTotal() {
        return maxConnectTotal;
    }

    public void setMaxConnectTotal(Integer maxConnectTotal) {
        this.maxConnectTotal = maxConnectTotal;
    }

    public Integer getMaxConnectPerRoute() {
        return maxConnectPerRoute;
    }

    public void setMaxConnectPerRoute(Integer maxConnectPerRoute) {
        this.maxConnectPerRoute = maxConnectPerRoute;
    }

    public Integer getConnectionRequestTimeoutMillis() {
        return connectionRequestTimeoutMillis;
    }

    public void setConnectionRequestTimeoutMillis(Integer connectionRequestTimeoutMillis) {
        this.connectionRequestTimeoutMillis = connectionRequestTimeoutMillis;
    }

    public Integer getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(Integer socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    public Integer getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(Integer connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
