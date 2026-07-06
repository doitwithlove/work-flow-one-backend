package com.touchmind.work.flow.one.sample.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Component
public class SampleDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleDataSeeder.class);
    private static final Set<String> TEMPORAL_FIELDS = Set.of(
            "timestamp",
            "createdAt",
            "updatedAt",
            "loginTime",
            "logoutTime",
            "lastServiceAt",
            "nextServiceDueAt");

    private final ReactiveMongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    private final SampleDataSeederProperties properties;

    public SampleDataSeeder(
            ReactiveMongoTemplate mongoTemplate,
            ObjectMapper objectMapper,
            SampleDataSeederProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            log.info("Sample data seeding is disabled.");
            return;
        }

        List<Path> sourceDirs = Arrays.stream(properties.getSourceDirs().split(","))
                .map(String::trim)
                .filter(dir -> !dir.isBlank())
                .map(Path::of)
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .toList();

        Flux.fromIterable(sourceDirs)
                .concatMap(this::seedSourceDirectory)
                .then()
                .doOnSuccess(unused -> log.info("Sample data seeded from {} source directories", sourceDirs.size()))
                .doOnError(error -> log.warn("Sample data seeding failed: {}", error.getMessage(), error))
                .block();
    }

    private Mono<Void> seedSourceDirectory(Path sourceDir) {
        Path machineFile = sourceDir.resolve("mongodb_machine_model.json");
        Path collectionsFile = sourceDir.resolve("mongodb_collections.json");

        if (!Files.exists(machineFile) || !Files.exists(collectionsFile)) {
            log.info("Sample data files not found at {}. Skipping seed.", sourceDir);
            return Mono.empty();
        }

        Mono<JsonNode> machineRootMono = Mono.fromCallable(() -> objectMapper.readTree(machineFile.toFile()));
        Mono<JsonNode> collectionsRootMono = Mono.fromCallable(() -> objectMapper.readTree(collectionsFile.toFile()));

        return machineRootMono.flatMap(machineRoot -> {
            Set<String> machineIds = machineIds(machineRoot);
            return seedMachine(machineRoot)
                    .then(collectionsRootMono.flatMap(collectionsRoot -> seedCollections(collectionsRoot, machineIds)));
        }).doOnSuccess(unused -> log.info("Sample data seeded from {}", sourceDir));
    }

    private Mono<Void> seedMachine(JsonNode root) {
        JsonNode documentNode = root.get("document");
        JsonNode variantsNode = root.get("variants");
        if (documentNode == null || documentNode.isNull()) {
            return Mono.empty();
        }

        List<Document> machineDocuments = new ArrayList<>();
        machineDocuments.add(toDocument(documentNode));
        if (variantsNode != null && variantsNode.isArray()) {
            variantsNode.forEach(node -> machineDocuments.add(toDocument(node)));
        }

        List<String> machineIds = machineDocuments.stream()
                .map(document -> document.getString("machineId"))
                .filter(id -> id != null && !id.isBlank())
                .toList();

        Query query = Query.query(Criteria.where("machineId").in(machineIds));
        return mongoTemplate.remove(query, "machines")
                .thenMany(Flux.fromIterable(machineDocuments))
                .flatMap(document -> mongoTemplate.insert(document, "machines"))
                .then();
    }

    private Mono<Void> seedCollections(JsonNode root, Set<String> machineIds) {
        if (!root.isObject()) {
            return Mono.empty();
        }

        Flux<Map.Entry<String, JsonNode>> imports = Flux.fromStream(
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(root.fields(), Spliterator.ORDERED), false));

        return imports
                .filter(entry -> !"machines".equals(entry.getKey()))
                .flatMap(entry -> seedCollection(entry.getKey(), entry.getValue(), machineIds))
                .then();
    }

    private Mono<Void> seedCollection(String collection, JsonNode documentsNode, Set<String> machineIds) {
        if (documentsNode == null || !documentsNode.isArray() || documentsNode.isEmpty()) {
            return Mono.empty();
        }

        Query removeQuery = machineIds.isEmpty()
                ? new Query()
                : Query.query(Criteria.where("machineId").in(machineIds));
        return mongoTemplate.remove(removeQuery, collection)
                .thenMany(Flux.fromStream(
                                StreamSupport.stream(Spliterators.spliteratorUnknownSize(documentsNode.elements(), Spliterator.ORDERED), false))
                        .map(this::toDocument)
                        .flatMap(document -> mongoTemplate.insert(document, collection)))
                .then();
    }

    private Set<String> machineIds(JsonNode root) {
        Set<String> machineIds = new LinkedHashSet<>();
        JsonNode documentNode = root.get("document");
        JsonNode variantsNode = root.get("variants");
        if (documentNode != null && documentNode.hasNonNull("machineId")) {
            machineIds.add(documentNode.get("machineId").asText());
        }
        if (variantsNode != null && variantsNode.isArray()) {
            variantsNode.forEach(node -> {
                if (node.hasNonNull("machineId")) {
                    machineIds.add(node.get("machineId").asText());
                }
            });
        }
        return machineIds;
    }

    private Document toDocument(JsonNode node) {
        Document document = new Document();
        node.fields().forEachRemaining(entry -> document.put(entry.getKey(), toValue(entry.getKey(), entry.getValue())));
        document.remove("_id");
        return document;
    }

    private Object toValue(String fieldName, JsonNode node) {
        if (node.isObject()) {
            Document document = new Document();
            node.fields().forEachRemaining(entry -> document.put(entry.getKey(), toValue(entry.getKey(), entry.getValue())));
            return document;
        }

        if (node.isArray()) {
            ArrayList<Object> values = new ArrayList<>();
            node.forEach(child -> values.add(toValue(fieldName, child)));
            return values;
        }

        if (TEMPORAL_FIELDS.contains(fieldName) && node.isTextual()) {
            return Date.from(parseTemporal(node.asText()));
        }

        if (node.isTextual()) {
            return node.asText();
        }

        if (node.isNumber()) {
            return node.numberValue();
        }

        if (node.isBoolean()) {
            return node.booleanValue();
        }

        if (node.isNull()) {
            return null;
        }

        return node.asText();
    }

    private Instant parseTemporal(String value) {
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            // Fall through to local-date/time parsing.
        }

        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (Exception ignored) {
            // Fall through to local date parsing.
        }

        return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
