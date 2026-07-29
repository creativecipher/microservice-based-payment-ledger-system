package com.coder.api_gateway.apiSecurityPkg.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    // List of open, public endpoints that bypass JWT validation
    public static final List<String> openApiEndpints = List.of(
            "/customer/register-customer",
            "/customer-auth/login",
            "/eureka"
    );
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
