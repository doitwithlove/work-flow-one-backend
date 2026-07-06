package com.touchmind.work.flow.one.manufacturing.camel;

import com.touchmind.work.flow.one.manufacturing.dto.MachineEventRequest;
import com.touchmind.work.flow.one.manufacturing.dto.MachineEventResponse;
import com.touchmind.work.flow.one.manufacturing.service.MachineEventService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ManufacturingRouteConfig extends RouteBuilder {

    private final MachineEventService machineEventService;

    public ManufacturingRouteConfig(MachineEventService machineEventService) {
        this.machineEventService = machineEventService;
    }

    @Override
    public void configure() {
        from("direct:manufacturing-machine-event")
                .routeId("manufacturing-machine-event-route")
                .process(exchange -> {
                    MachineEventRequest request = exchange.getMessage().getBody(MachineEventRequest.class);
                    MachineEventResponse response = machineEventService.ingest(request).block();
                    exchange.getMessage().setBody(response);
                });

        from("direct:manufacturing-machine-event-list")
                .routeId("manufacturing-machine-event-list-route")
                .process(exchange -> exchange.getMessage().setBody(machineEventService.list().collectList().block()));

        from("direct:manufacturing-machine-event-part-list")
                .routeId("manufacturing-machine-event-part-list-route")
                .process(exchange -> {
                    String partId = exchange.getMessage().getBody(String.class);
                    exchange.getMessage().setBody(machineEventService.listByPart(partId).collectList().block());
                });
    }
}
