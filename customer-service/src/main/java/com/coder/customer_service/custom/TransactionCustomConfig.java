package com.coder.customer_service.custom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TransactionCustomConfig {
    @Bean
    public RestTemplate getRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();

        // Always set timeouts so your app doesn't hang indefinitely
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // set 5 second to connect
        factory.setReadTimeout(5000); // set 5 second to read data

        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }
}
