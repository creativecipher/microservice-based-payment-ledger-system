package com.coder.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Global Logging / Auth Pre-Filter (Optional settings)
 * Help to inspect or intercept every request entering your ecosystem by creating a GlobalFilter
 * **/
@Component
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    // inherited from GlobalFilter
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("incoming Request: Method={}, Path={}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath());
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            log.info("Response Status Code: {}",exchange.getResponse().getStatusCode());
        }));
    }

    // inherited from Ordered
    @Override
    public int getOrder() {
        return -1; // High priority execution
    }
}
