package com.coder.account_service.custom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AccountCustomConfig {

    @Bean
    RestTemplate getRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();

        // Production Best Practice : Always set timeouts so your app doesn't hang indefinitely
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds to connect
        factory.setReadTimeout(5000);    // 5 seconds to read data

        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }
}
