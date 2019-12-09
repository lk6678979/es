package com.lk.es.restfull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
public class ElasticsearchRestClientConfig {

    /**
     * 使用冒号隔开ip和端口1
     */
    @Value("${spring.elasticsearch.rest.cluster-nodes}")
    private String[] ipAddress;

    /**
     * 连接超时是时间
     */
    @Value("${spring.elasticsearch.rest.connect-timeout}")
    private int connectTimeout;

    /**
     * 套接字连接超时是时间
     */
    @Value("${spring.elasticsearch.rest.socket-timeout}")
    private int socketTimeout;

    /**
     * 异步请求线程数
     */
    @Value("${spring.elasticsearch.rest.io-thread-count}")
    private int ioThreadCount;

    private static final int ES_IPADDRS_LENGTH = 2;
    private static final String HTTP_SCHEME = "http";

    @Bean
    public RestClientBuilder restClientBuilder() {
        HttpHost[] hosts = Arrays.stream(ipAddress)
                .map(this::makeHttpHost)
                .filter(Objects::nonNull)
                .toArray(HttpHost[]::new);
        RestClientBuilder clientBuilder = RestClient.builder(hosts);
        // 1.设置请求头
        Header[] defaultHeaders = {new BasicHeader("header", "value")};
        clientBuilder.setDefaultHeaders(defaultHeaders);
        /**
         *3.设置失败监听器，
         *每次节点失败都可以监听到，可以作额外处理
         */
        clientBuilder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                super.onFailure(node);
                System.out.println(node.getName() + "==节点失败了");
            }
        });

        /** 4.配置节点选择器，客户端以循环方式将每个请求发送到每一个配置的节点上，
         *发送请求的节点，用于过滤客户端，将请求发送到这些客户端节点，默认向每个配置节点发送，
         *这个配置通常是用户在启用嗅探时向专用主节点发送请求（即只有专用的主节点应该被HTTP请求命中）
         */
        clientBuilder.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);

        /**5. 配置异步请求的线程数量，Apache Http Async Client默认启动一个调度程序线程，以及由连接管理器使用的许多工作线程
         *（与本地检测到的处理器数量一样多，取决于Runtime.getRuntime().availableProcessors()返回的数量）。线程数可以修改如下,
         *这里是修改为1个线程，即默认情况
         */
        clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                return httpAsyncClientBuilder.setDefaultIOReactorConfig(
                        IOReactorConfig.custom().setIoThreadCount(ioThreadCount).build()
                );
            }
        });
        /**
         *6. 配置连接超时和套接字超时
         *配置请求超时，将连接超时（默认为1秒）和套接字超时（默认为30秒）增加，
         */
        clientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                // 连接5秒超时，套接字连接60s超时
                return requestConfigBuilder.setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout);
            }
        });
        return clientBuilder;
    }

    @Bean(name = "highLevelClient")
    public RestHighLevelClient highLevelClient(RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }


    private HttpHost makeHttpHost(String s) {
        assert StringUtils.isNotEmpty(s);
        String[] address = s.split(":");
        if (address.length == ES_IPADDRS_LENGTH) {
            String ip = address[0];
            int port = Integer.parseInt(address[1]);
            return new HttpHost(ip, port, HTTP_SCHEME);
        }
        return null;
    }

    @Bean
    public RestClient getClient(RestClientBuilder restClientBuilder) {
        return restClientBuilder.build();
    }
}