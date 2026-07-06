package com.touchmind.work.flow.one.sample.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class IndustrialSampleIngestionService {

    private final ReactiveMongoTemplate mongoTemplate;

    public IndustrialSampleIngestionService(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<Void> ingestKafkaJsonl(Path file) {
        return readLines(file)
                .map(Document::parse)
                .flatMap(document -> mongoTemplate.insert(document, "kafkaEvents"))
                .then();
    }

    public Mono<Void> ingestCsv(Path file) {
        String collection = collectionName(file.getFileName().toString());
        return readCsv(file)
                .flatMap(document -> mongoTemplate.insert(document, collection))
                .then();
    }

    private Flux<String> readLines(Path file) {
        return Flux.using(
                () -> Files.newBufferedReader(file, StandardCharsets.UTF_8),
                reader -> Flux.fromStream(reader.lines()),
                this::closeReader);
    }

    private Flux<Document> readCsv(Path file) {
        return Flux.using(
                () -> Files.newBufferedReader(file, StandardCharsets.UTF_8),
                reader -> {
                    String headerLine;
                    try {
                        headerLine = reader.readLine();
                    } catch (IOException ex) {
                        return Flux.error(ex);
                    }
                    if (headerLine == null) {
                        return Flux.empty();
                    }
                    List<String> headers = parseCsvLine(headerLine);
                    return Flux.fromStream(reader.lines())
                            .map(this::parseCsvLine)
                            .map(values -> toDocument(headers, values));
                },
                this::closeReader);
    }

    private Document toDocument(List<String> headers, List<String> values) {
        Document document = new Document();
        int size = Math.min(headers.size(), values.size());
        for (int index = 0; index < size; index++) {
            document.put(headers.get(index), coerce(values.get(index)));
        }
        return document;
    }

    private Object coerce(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return Boolean.parseBoolean(trimmed);
        }
        try {
            if (trimmed.contains(".")) {
                return Double.parseDouble(trimmed);
            }
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            return trimmed;
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char ch = line.charAt(index);
            if (ch == '"' ) {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }

    private String collectionName(String filename) {
        return switch (filename) {
            case "production_telemetry.csv" -> "productionTelemetry";
            case "alarm_history.csv" -> "alarms";
            case "oee_shift_summary.csv" -> "oeeReports";
            case "tool_wear_predictive.csv" -> "tools";
            case "quality_inspection.csv" -> "qualityInspections";
            case "energy_cycle.csv" -> "energyCycles";
            case "operator_shift_logs.csv" -> "operatorShiftLogs";
            default -> "sampleData";
        };
    }

    private void closeReader(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
