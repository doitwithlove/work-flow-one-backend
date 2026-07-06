package com.touchmind.work.flow.one.sample.config;

import com.touchmind.work.flow.one.sample.service.IndustrialSampleIngestionService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class IndustrialSampleRouteConfig extends RouteBuilder {

    private final IndustrialSampleIngestionService ingestionService;
    private final String inputDir;

    public IndustrialSampleRouteConfig(
            IndustrialSampleIngestionService ingestionService,
            @Value("${turmik.ingest.input-dir:${user.home}/turmik-q15-ingest}") String inputDir) {
        this.ingestionService = ingestionService;
        this.inputDir = inputDir;
    }

    @Override
    public void configure() {
        from("file:" + inputDir + "/jsonl?noop=true&include=kafka_events\\.jsonl")
                .routeId("turmik-q15-jsonl-ingest")
                .process(this::ingestJsonl);

        from("file:" + inputDir + "/csv?noop=true&include=.*\\.csv")
                .routeId("turmik-q15-csv-ingest")
                .process(this::ingestCsv);
    }

    private void ingestJsonl(Exchange exchange) {
        File file = exchange.getIn().getBody(File.class);
        ingestionService.ingestKafkaJsonl(file.toPath()).subscribe();
    }

    private void ingestCsv(Exchange exchange) {
        File file = exchange.getIn().getBody(File.class);
        ingestionService.ingestCsv(file.toPath()).subscribe();
    }
}
