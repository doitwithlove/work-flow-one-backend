package com.touchmind.work.flow.one.config;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class SpaFallbackWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (shouldForwardToIndex(request.getMethod(), path)) {
            ServerWebExchange forwarded = exchange.mutate()
                .request(request.mutate().path("/index.html").build())
                .build();
            return chain.filter(forwarded);
        }

        return chain.filter(exchange);
    }

    private boolean shouldForwardToIndex(HttpMethod method, String path) {
        if (method != HttpMethod.GET || path == null || path.isBlank()) {
            return false;
        }

        if (path.startsWith("/api/") || path.startsWith("/actuator/") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/webjars")) {
            return false;
        }

        return !path.contains(".");
    }
}
