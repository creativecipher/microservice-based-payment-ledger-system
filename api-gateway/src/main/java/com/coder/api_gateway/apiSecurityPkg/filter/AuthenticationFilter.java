package com.coder.api_gateway.apiSecurityPkg.filter;


import com.coder.api_gateway.apiSecurityPkg.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouteValidator validator, JwtUtil jwtUtil){
        super(Config.class);
        this.validator=validator;
        this.jwtUtil=jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (((exchange, chain) -> {
            // 1. Check if the current request is for a secured endpoint
            if(validator.isSecured.test(exchange.getRequest())){
                // 2. Check if Authorization header is present
                if(!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if(authHeader!=null && authHeader.startsWith("Bearer ")){
                    authHeader = authHeader.substring(7);
                }else{
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // 3. Validate Token
                try{
                    jwtUtil.validateToken(authHeader);

                    // Optional Fintech Best Practice:
                    // Extract user ID from token and mutate the request header so downstream services know WHO is making the request
                    String username = jwtUtil.extractUsername(authHeader);
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate().header("X-Auth-User-Id",username).build())
                            .build();

                    return chain.filter(modifiedExchange);
                }catch (Exception e){
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        }));
    }

    public static class Config{
        // configuration properties if needed
    }
}
