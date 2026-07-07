/*
 * Example: Consuming NetGauze YANG-Push Kafka messages in yangkit (Java)
 *
 * NetGauze (Rust) produces Kafka records with:
 *   Header "schema-id"    = UTF-8 string of the Confluent Schema Registry integer ID, e.g. "42"
 *   Header "content-type" = "application/yang.data+json"
 *   Payload               = raw RFC 7951 YANG JSON bytes (no Confluent magic byte prefix)
 *
 * Local disk cache written by NetGauze:
 *   <cache_root>/
 *     <content-id>/           e.g. "schema-id-42"
 *       yang-lib.xml          RFC 8525 YANG Library XML
 *       modules/              YANG source files (.yang)
 *         module@revision.yang
 *         ...
 *       subscriptions-info.json
 *
 * Two approaches are shown:
 *   Approach A – Using yang-kafka-integration KafkaYangJsonSchemaDeserializer
 *                (reads schema from Confluent Schema Registry at runtime)
 *   Approach B – Using yangkit YangLibraryParser + local cache
 *                (no Confluent SR needed at consumer time; schemas from disk)
 */

package org.yangcentral.yangkit.examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.register.YangStatementImplRegister;

/**
 * Demonstrates consuming YANG-Push telemetry messages produced by NetGauze into Kafka.
 *
 * <p><b>Prerequisites</b>:
 * <ol>
 *   <li>NetGauze is running and publishing to the Kafka topic.
 *   <li>The NetGauze local disk cache is accessible from this process
 *       (or mounted, or copied to a shared path).
 *   <li>yangkit jars on the classpath (yangkit-parser, yangkit-model-*, yangkit-data-*).
 * </ol>
 */
public class NetGauzeYangKafkaConsumer {

    // ── Kafka configuration ──────────────────────────────────────────────────

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC             = "yang-push-telemetry";
    private static final String GROUP_ID          = "yangkit-consumer-group";

    // ── Confluent Schema Registry (Approach A only) ──────────────────────────

    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";

    // ── NetGauze local disk cache root (Approach B) ──────────────────────────
    // This is the same path set in NetGauze's collector config as cache_root_path.

    private static final String NETGAUZE_CACHE_ROOT = "/var/lib/netgauze/yang-cache";

    // ── Header key constants (same as NetGauze Rust code) ───────────────────

    private static final String HEADER_SCHEMA_ID    = "schema-id";
    private static final String HEADER_CONTENT_TYPE = "content-type";
    private static final String CONTENT_TYPE_YANG   = "application/yang.data+json";

    // ── Schema context cache (reuse parsed schemas across records) ───────────

    private final Map<Integer, YangSchemaContext> schemaCache = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ═════════════════════════════════════════════════════════════════════════
    // APPROACH B – local cache only (recommended when cache is co-located)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Main consumer loop using the local NetGauze disk cache.
     * No Confluent Schema Registry client is needed.
     */
    public void runLocalCacheConsumer() throws Exception {

        YangStatementImplRegister.registerImpl();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ByteArrayDeserializer.class.getName());

        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            while (true) {
                ConsumerRecords<String, byte[]> records =
                        consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, byte[]> record : records) {
                    processRecordFromLocalCache(record);
                }
            }
        }
    }

    /**
     * Processes a single Kafka record using schemas from NetGauze's local disk cache.
     *
     * <h3>Flow</h3>
     * <ol>
     *   <li>Read {@code schema-id} header → integer (NetGauze writes as UTF-8 string)</li>
     *   <li>Compute content-id used by NetGauze: {@code "schema-id-<N>"}</li>
     *   <li>Load {@code yang-lib.xml} + {@code modules/} from the local cache</li>
     *   <li>Parse and validate the YANG JSON payload</li>
     * </ol>
     */
    private void processRecordFromLocalCache(ConsumerRecord<String, byte[]> record) {

        // ── 1. Extract schema-id from header (NetGauze writes as UTF-8 string) ──
        int schemaId = extractSchemaId(record);
        if (schemaId < 0) {
            System.err.println("Skipping record – missing or invalid schema-id header");
            return;
        }

        try {
            // ── 2. Get or build the schema context ──────────────────────────────
            YangSchemaContext schemaContext = schemaCache.computeIfAbsent(
                    schemaId, id -> loadSchemaFromLocalCache(id));

            if (schemaContext == null) {
                System.err.printf("Cannot load schema for schema-id=%d, skipping record%n",
                        schemaId);
                return;
            }

            // ── 3. Parse the YANG JSON payload ──────────────────────────────────
            JsonNode jsonPayload = objectMapper.readTree(record.value());
            YangDataDocument doc = parseYangJson(schemaContext, jsonPayload);

            if (doc != null) {
                handleYangDocument(doc, schemaId, record.partition(), record.offset());
            }

        } catch (IOException e) {
            System.err.printf(
                    "Failed to parse YANG JSON for schema-id=%d offset=%d: %s%n",
                    schemaId, record.offset(), e.getMessage());
        }
    }

    /**
     * Reads and caches a {@link YangSchemaContext} from NetGauze's local disk cache.
     *
     * <p>NetGauze names the content-id directory {@code "schema-id-<N>"} where N is
     * the Confluent Schema Registry integer ID.
     *
     * <p>Expected directory layout:
     * <pre>
     * {NETGAUZE_CACHE_ROOT}/
     *   schema-id-42/
     *     yang-lib.xml      ← RFC 8525 YANG Library XML
     *     modules/          ← YANG source files
     *       ietf-interfaces@2018-02-20.yang
     *       ...
     * </pre>
     *
     * @param schemaId the integer ID from the {@code schema-id} Kafka header
     * @return parsed and validated {@link YangSchemaContext}, or {@code null} on error
     */
    private YangSchemaContext loadSchemaFromLocalCache(int schemaId) {
        // NetGauze content-id naming: "schema-id-<N>"
        String contentId   = "schema-id-" + schemaId;
        File   yangLibXml  = new File(NETGAUZE_CACHE_ROOT, contentId + "/yang-lib.xml");
        File   modulesDir  = new File(NETGAUZE_CACHE_ROOT, contentId + "/modules");

        if (!yangLibXml.exists()) {
            System.err.printf(
                    "yang-lib.xml not found at %s – is the NetGauze cache accessible?%n",
                    yangLibXml.getAbsolutePath());
            return null;
        }
        if (!modulesDir.isDirectory()) {
            System.err.printf(
                    "modules/ directory not found at %s%n", modulesDir.getAbsolutePath());
            return null;
        }

        try {
            System.out.printf(
                    "Loading YANG schema for schema-id=%d from %s%n",
                    schemaId, yangLibXml.getAbsolutePath());

            // YangYinParser.parseFromYangLibrary() was added in yangkit to support
            // RFC 8525 YANG Library XML → YangSchemaContext. See YangLibraryParser.java.
            YangSchemaContext ctx = YangYinParser.parseFromYangLibrary(yangLibXml, modulesDir);
            ctx.validate();

            System.out.printf(
                    "Schema loaded for schema-id=%d – %d modules%n",
                    schemaId, ctx.getModules().size());
            return ctx;

        } catch (Exception e) {
            System.err.printf(
                    "Error loading YANG Library for schema-id=%d: %s%n",
                    schemaId, e.getMessage());
            return null;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // APPROACH A – Confluent Schema Registry deserializer
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Example of Approach A: using {@code KafkaYangJsonSchemaDeserializer} from
     * yang-kafka-integration. This fetches YANG schemas from Confluent Schema Registry
     * at runtime. The {@code schema-id} header incompatibility (NetGauze writes UTF-8
     * string, Java originally expected 4-byte binary) has been fixed in
     * {@code AbstractKafkaYangJsonSchemaDeserializer}.
     *
     * <p>Add to your pom.xml:
     * <pre>{@code
     * <dependency>
     *   <groupId>ch.swisscom.kafka</groupId>
     *   <artifactId>yang-json-schema-serializer</artifactId>
     *   <version>0.0.5</version>
     * </dependency>
     * }</pre>
     *
     * <p>Kafka consumer configuration:
     * <pre>{@code
     * props.put("schema.registry.url", "http://localhost:8081");
     * props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
     *           "ch.swisscom.kafka.serializers.yang.json.KafkaYangJsonSchemaDeserializer");
     * props.put("yang.json.fail.invalid.schema", false); // or true for strict validation
     * }</pre>
     *
     * <p>The deserializer returns {@link YangDataDocument} values directly.
     */
    public static Map<String, Object> buildSchemaRegistryConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID + "-sr");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        // Deserializer from yang-kafka-integration (fixed to handle UTF-8 schema-id)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "ch.swisscom.kafka.serializers.yang.json.KafkaYangJsonSchemaDeserializer");
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        // Validate YANG JSON against the schema on each message
        props.put("yang.json.fail.invalid.schema", false);
        return props;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Shared helpers
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Extracts the {@code schema-id} integer from Kafka headers.
     *
     * <p>NetGauze writes this header as a UTF-8 decimal string (e.g. {@code "42"}).
     * This is different from the Confluent Java convention of 4-byte big-endian binary.
     *
     * @return the schema ID, or -1 if missing/unparseable
     */
    private int extractSchemaId(ConsumerRecord<?, ?> record) {
        Header header = record.headers().lastHeader(HEADER_SCHEMA_ID);
        if (header == null || header.value() == null) {
            return -1;
        }
        try {
            // NetGauze writes: id.to_string() → UTF-8 string bytes
            return Integer.parseInt(
                    new String(header.value(), StandardCharsets.UTF_8).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Verifies the content-type header is YANG JSON.
     */
    private boolean isYangJson(ConsumerRecord<?, ?> record) {
        Header header = record.headers().lastHeader(HEADER_CONTENT_TYPE);
        if (header == null || header.value() == null) {
            return false;
        }
        String contentType = new String(header.value(), StandardCharsets.UTF_8);
        return contentType.startsWith(CONTENT_TYPE_YANG)
                || contentType.startsWith("application/yang-data+json"); // alternate form
    }

    /**
     * Parses a YANG JSON payload using the provided schema context.
     * Returns the validated {@link YangDataDocument}, or {@code null} on failure.
     */
    private YangDataDocument parseYangJson(YangSchemaContext schemaContext, JsonNode jsonPayload) {
        try {
            org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser parser =
                    new org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser(
                            schemaContext);
            org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder resultBuilder =
                    new org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder();
            YangDataDocument doc = parser.parse(jsonPayload, resultBuilder);
            if (!resultBuilder.build().isOk()) {
                System.err.println("YANG validation errors: " + resultBuilder.build().getRecords());
            }
            return doc;
        } catch (Exception e) {
            System.err.println("Error parsing YANG JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handle the successfully parsed YANG document.
     * Override / extend this to forward the document to your application logic.
     */
    private void handleYangDocument(YangDataDocument doc, int schemaId,
                                    int partition, long offset) {
        System.out.printf(
                "[partition=%d offset=%d schema-id=%d] Received YANG document with %d root nodes%n",
                partition, offset, schemaId,
                doc.getDataChildren() == null ? 0 : doc.getDataChildren().size());
        // TODO: pass doc to your processing logic
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Entry point
    // ═════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) throws Exception {
        YangStatementImplRegister.registerImpl();

        NetGauzeYangKafkaConsumer consumer = new NetGauzeYangKafkaConsumer();
        System.out.println("Starting NetGauze YANG Kafka consumer (local cache mode)...");
        consumer.runLocalCacheConsumer();
    }
}
