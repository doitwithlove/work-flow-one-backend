# Turmik Q15 Industrial Sample Data Package

This package contains synthetic but realistic sample data for an Askar Turmik Q15 CNC turn-mill machine.

## Files

- `production_telemetry.csv` - high-volume telemetry for machine states, motion, load, temperature, vibration, and part counts.
- `alarm_history.csv` - alarm log with severity, duration, and operator recovery actions.
- `oee_shift_summary.csv` - calculated OEE by shift for planning and performance analysis.
- `tool_wear_predictive.csv` - predictive maintenance data for tool wear and remaining life estimation.
- `quality_inspection.csv` - dimensional quality checks and surface finish inspections.
- `energy_cycle.csv` - per-cycle energy consumption data for power analysis.
- `operator_shift_logs.csv` - operator attendance, runtime, downtime, and shift notes.
- `kafka_events.jsonl` - event stream suitable for Kafka ingestion.
- `mongodb_collections.json` - MongoDB-style sample collections for documents and seed data.
- `mongodb_machine_model.json` - MongoDB seed model for the TURMIK-Q15 machine documents.
- `Turmik_Q15_Industrial_Sample_Datasets.xlsx` - workbook with one sheet per dataset.

## How to use this data

### Spring Boot

Use the CSV files as seed inputs for reactive imports with `WebFlux`, `ReactiveMongoRepository`, and Apache Camel file routes.
Typical targets:

- load `production_telemetry.csv` into a `productionTelemetry` collection
- map `alarm_history.csv` into an alarms collection
- use `oee_shift_summary.csv` for dashboard APIs

### MongoDB

The `mongodb_collections.json` file is shaped like application seed documents. The `mongodb_machine_model.json` file is a direct seed model for the `machines` collection. They are useful for:

- local seed scripts
- integration tests
- sample dashboards
- validating indexes on `machineId`, `timestamp`, `jobId`, and `shift`
- demonstrating multiple machine profiles and job families in a single dataset

### Apache Camel

Camel can ingest these files from a local directory or ZIP extraction target:

- route CSV files to MongoDB
- publish JSONL events to Kafka
- transform and enrich telemetry records before persistence

### Kafka

`kafka_events.jsonl` is ready for line-by-line event streaming. A common pattern is:

- use `machineId` as the message key
- publish `TELEMETRY_RECORDED`, `ALARM_RAISED`, and `PART_COMPLETED` events
- route the stream into a metrics pipeline or alerting service

### React dashboards

The data is suitable for:

- live charts for spindle RPM, feed rate, load, temperature, and vibration
- OEE cards and shift comparisons
- alarm timelines
- tool wear prediction widgets
- quality control tables and pass/fail summaries

## Notes

- The primary machine ID is fixed to `TURMIK-Q15-01`, with additional machine profiles included for variant testing.
- The telemetry spans seven operational days starting at the configured start date in the generator script.
- The generator is deterministic when the seed constant is unchanged.
