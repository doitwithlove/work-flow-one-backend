package com.touchmind.work.flow.one.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CamelRouteConfig extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:audit")
                .routeId("auth-audit-route")
                .log("auth-event: ${body}");
    }
}
