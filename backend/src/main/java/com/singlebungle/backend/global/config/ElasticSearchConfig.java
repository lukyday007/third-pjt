package com.singlebungle.backend.global.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import java.util.List;

@Configuration
public class ElasticSearchConfig {

//    @Value("${spring.elasticsearch.username}")
//    private String username;
//    @Value("${spring.elasticsearch.password}")
//    private String password;

    // k11b205.p.ssafy.io

    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("k11b205.p.ssafy.io", 9200, "http")
                )
        );
    }

//    @Value("${spring.elasticsearch.uris}")
//    private String elasticUrl;
//
//    @Override
//    public ClientConfiguration clientConfiguration() {
//        return ClientConfiguration.builder()
//                .connectedTo(elasticUrl)
//                .build();
//    }
}


